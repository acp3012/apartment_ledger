package com.yesh.apartmentledger.security.authorization;

import com.yesh.apartmentledger.security.model.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("apartmentAuthorization")
public class ApartmentAuthorization {

    public boolean canAccess(Long requestedApartmentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || requestedApartmentId == null) {
            return false;
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        return authentication.getPrincipal() instanceof CustomUserDetails userDetails
                && userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"))
                && Objects.equals(userDetails.getApartmentId(), requestedApartmentId);
    }
}