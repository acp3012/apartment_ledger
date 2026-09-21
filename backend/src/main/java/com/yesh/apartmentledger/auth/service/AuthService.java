package com.yesh.apartmentledger.auth.service;

import com.yesh.apartmentledger.auth.dto.AuthResponse;
import com.yesh.apartmentledger.auth.dto.LoginRequest;
import com.yesh.apartmentledger.auth.dto.RegisterRequest;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.core.user.repository.AppUserRepository;
import com.yesh.apartmentledger.exception.BadRequestException;

import com.yesh.apartmentledger.exception.ResourceNotFoundException;
import com.yesh.apartmentledger.master.role.entity.Role;
import com.yesh.apartmentledger.master.role.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.ReadOnlyFileSystemException;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final FlatRepository flatRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final String DEFAULT_USER = "USER";
    private final String ADMIN_USER = "ADMIN";

    public AuthService(AppUserRepository appUserRepository,
                       FlatRepository flatRepository,
                       PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository
                       )      {

        this.appUserRepository = appUserRepository;
        this.flatRepository = flatRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
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

        // 4. Optionally generate a token immediately upon registration, or force them to log in.
        return new AuthResponse(
                user.getId(),
                null,
                user.getDisplayName(),
                user.getEmail(),
                flat.getApartment().getId(),
                flat.getApartment().getApartmentName(),
                flat.getId(),
                flat.getFlatNumber(),
                defaultRole.getRoleName().equalsIgnoreCase(ADMIN_USER),
                "Registration successful. Please log in."
        );
    }

    @Transactional(readOnly = true) // <--To avoid Lazyloading error
    public AuthResponse login(LoginRequest request) {
        // 1. Fetch the user manually from the database
        AppUser user;
        user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email Id password."));

        // 2. Manually check if the password matches the hash in the database
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email id or password.");
        }
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(ADMIN_USER));
        // 3. Return the user data so React can load the dashboard.
        // We pass a fake "MVP" token so your React app doesn't break if it expects one.
        return new AuthResponse(
                user.getId(),
                "mvp-dummy-token-no-auth",
                user.getDisplayName(),
                user.getEmail(),
                user.getApartment().getId(),
                user.getApartment().getApartmentName(),
                user.getFlat().getId(),
                user.getFlat().getFlatNumber(),
                isAdmin,
                "Login successful (MVP Mode)."
        );
    }
}