package com.yesh.apartmentledger.auth.service;

import com.yesh.apartmentledger.core.user.repository.AppUserRepository;
import com.yesh.apartmentledger.security.model.CustomUserDetails;
import org.jspecify.annotations.NonNull;
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
        // 1. Fetch the user from your database using the email
        AppUser appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserDetails(appUser);
    }
}