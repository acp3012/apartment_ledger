package com.yesh.apartmentledger.auth.service;

import com.yesh.apartmentledger.core.user.repository.AppUserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yesh.apartmentledger.core.user.entity.AppUser;

@Service // Tells Spring to register this as a bean
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public CustomUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Fetch the user from your database using the mobile number
        AppUser appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Convert your AppUser entity into a Spring Security User object
        return User.builder()
                .username(appUser.getMobileNumber()) // We use mobile number as the "username"
                .password(appUser.getPasswordHash())     // The hashed password from the database
                .roles("USER")                       // Assign a default role for now
                .build();
    }
}