package com.yesh.apartmentledger.security.model;

import com.yesh.apartmentledger.core.user.entity.AppUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter

public class CustomUserDetails implements UserDetails {

    private final String username; // We will use mobileNumber as the principal
    private final String password;
    private final Long apartmentId;
    private final Long flatId;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(AppUser appUser) {
        this.username = appUser.getEmail();
        this.password = appUser.getPasswordHash();
        this.apartmentId = appUser.getApartment().getId();
        this.flatId = appUser.getFlat() != null ? appUser.getFlat().getId() : null;

        // Map the user's role from the Master schema to a Spring Security authority

        this.authorities = appUser.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName().toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return username; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}