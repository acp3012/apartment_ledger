package com.yesh.apartmentledger.auth.service;

import com.yesh.apartmentledger.auth.dto.AuthResponse;
import com.yesh.apartmentledger.auth.dto.CreatePasswordRequest;
import com.yesh.apartmentledger.auth.dto.LoginRequest;
import com.yesh.apartmentledger.auth.dto.PasswordChangeRequest;
import com.yesh.apartmentledger.auth.dto.RegisterRequest;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.core.user.repository.AppUserRepository;
import com.yesh.apartmentledger.exception.BadRequestException;

import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.master.role.entity.Role;
import com.yesh.apartmentledger.master.role.repository.RoleRepository;
import com.yesh.apartmentledger.security.model.CustomUserDetails;
import com.yesh.apartmentledger.security.service.JwtService;
import lombok.AllArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final FlatRepository flatRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private static final String DEFAULT_USER = "USER";
    private static final String ADMIN_USER = "ADMIN";

    /**
     * User registration
     * @param request User register form

     */
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Check if email already exists to prevent duplicates
        if (appUserRepository.existsByEmail(request.email())) {
            throw new BadRequestException( "Email is already registered.");
        }

        // 2. Validate the shared secret (Owner's Mobile) against the Flat table
        Flat flat = flatRepository.findByOwnerMobile(request.ownerMobile())
                .orElseThrow(() -> new BadRequestException("Mobile number not registered to any flat. Please contact the Administrator."));
        // 3. Get default role_id
        var defaultRole = roleRepository.findByRoleNameIgnoreCase(DEFAULT_USER)
                .orElseThrow(()-> new ResourceNotFoundException("Default  role [" + DEFAULT_USER + "] does not exists in database."));

        // 3. Create the new user and securely map the IDs
        var user = new AppUser();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setMobileNumber(request.ownerMobile()); // Storing the shared secret as their login ID
        user.setDisplayName(request.displayName());
        user.setApartment(flat.getApartment());
        user.setFlat(flat);

        user.getRoles().add(defaultRole);

        // Note: In a full production flow, you would also insert a record into core.user_role here.
        user.setIsActive(true);
        user.setIsEmailVerified(false);

        appUserRepository.save(user);

    }
    @Transactional (readOnly = true) 
    
    public AuthResponse firstTimeAdminLogin(String email) {
        
        AppUser user = isFirstTimeAdminUser(email);
        
        return createAuthResponse(user, null);
    } 

     public void createAdminPassword(CreatePasswordRequest request) {
        AppUser user = isFirstTimeAdminUser(request.emailId());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedDate(java.time.LocalDateTime.now());
        appUserRepository.save(user);
    }
    /**
     * User login
     * @param request LoginRequest
     * @return AuthResponse
     */
    @Transactional(readOnly = true) 
    public AuthResponse login(LoginRequest request) {
      
        AppUser user;
        user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email Id or password."));

        // 2. Manually check if the password matches the hash in the database
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email id or password.");
        }
        if (!Boolean.TRUE.equals(user.getIsActive()) || Boolean.TRUE.equals(user.getIsLocked())) {
            throw new BadRequestException("This account is inactive or locked.");
        }
        return createAuthResponse(user, user.getFlat());
    }

 

    
    public  String hashPassword(String password) {
        // Implement your password hashing logic here
        // For example, you can use BCrypt or any other secure hashing algorithm
    
         return new BCryptPasswordEncoder().encode(password);
    }

    
    public void resetPassword(PasswordChangeRequest passwordChangeRequest) {
        
        AppUser user = appUserRepository.findByEmailAndIsActiveTrue(passwordChangeRequest.emailId())
                .orElseThrow(() -> new BadRequestException("Invalid email Id or the account is inactive."));

        if (!passwordEncoder.matches(passwordChangeRequest.oldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Old password didn't match");
        }
        if (passwordEncoder.matches(passwordChangeRequest.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Recently used passwords not allowed.");
        }


        user.setPasswordHash(passwordEncoder.encode(passwordChangeRequest.newPassword()));
        user.setPasswordChangedDate(java.time.LocalDateTime.now());
        
        appUserRepository.save(user);
    }

       private AuthResponse createAuthResponse(AppUser user, Flat flat) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equalsIgnoreCase(ADMIN_USER));
        String token = jwtService.generateToken(new CustomUserDetails(user));

        return new AuthResponse(
                user.getId(),
                token,
                user.getDisplayName(),
                user.getEmail(),
                user.getApartment().getId(),
                user.getApartment().getApartmentName(),
                flat != null ? flat.getId() : null,
                flat != null ? flat.getFlatNumber() : null,
                flat != null ? flat.getOwnerName() : null,
                isAdmin,
                Boolean.TRUE.equals(user.getApartment().getIsSetupCompleted()),
                user.getRoles().stream().map(Role::getRoleName).toList(),
                "Login successful."
        );
    }
    
    private AppUser isFirstTimeAdminUser(String emailId) {
       
        AppUser user = appUserRepository.findByEmail(emailId)
                .orElseThrow(() -> new BadRequestException("Invalid email Id."));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equalsIgnoreCase(ADMIN_USER));

        if (!isAdmin) {
            throw new BadRequestException("This account is not an admin.");
        }
        if(!(user.getPasswordChangedDate() == null)) {
            throw new BadRequestException("You are not a first time admin user. Please use the login API.");
        }
        // You can also generate a token or perform other actions as needed
        return user;
    }
   

}