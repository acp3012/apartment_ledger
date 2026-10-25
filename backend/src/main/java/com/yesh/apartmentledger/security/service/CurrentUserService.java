package com.yesh.apartmentledger.security.service;

import com.yesh.apartmentledger.core.user.entity.AppUser;
import com.yesh.apartmentledger.core.user.repository.AppUserRepository;
import com.yesh.apartmentledger.security.model.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public CustomUserDetails getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AuthenticationCredentialsNotFoundException("An authenticated user is required.");
        }
        return userDetails;
    }

    public AppUser getCurrentUser() {
        return appUserRepository.findById(getPrincipal().getUserId())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(
                        "The authenticated user no longer exists."));
    }

    public void requireApartmentAccess(Long apartmentId) {
        if (apartmentId == null || !Objects.equals(getPrincipal().getApartmentId(), apartmentId)) {
            throw new AccessDeniedException("You are not authorized to access this apartment.");
        }
    }
}