package com.arnav.authsystem.service;

import com.arnav.authsystem.entities.UserInfo;
import com.arnav.authsystem.entities.UserRole;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(UserInfo user) {

        this.username = user.getUsername();
        this.password = user.getPassword();

        List<GrantedAuthority> auths = new ArrayList<>();

        for (UserRole role : user.getRoles()) {
            auths.add(
                    new SimpleGrantedAuthority(
                            role.getName().toUpperCase()
                    )
            );
        }

        this.authorities = auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}