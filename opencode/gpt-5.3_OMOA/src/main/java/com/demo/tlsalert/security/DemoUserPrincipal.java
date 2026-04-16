package com.demo.tlsalert.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class DemoUserPrincipal implements UserDetails {

    private final String username;
    private final String password;
    private final String groupName;
    private final List<GrantedAuthority> authorities;

    public DemoUserPrincipal(String username, String password, String groupName, List<GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.groupName = groupName;
        this.authorities = List.copyOf(authorities);
    }

    public String getGroupName() {
        return groupName;
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
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
