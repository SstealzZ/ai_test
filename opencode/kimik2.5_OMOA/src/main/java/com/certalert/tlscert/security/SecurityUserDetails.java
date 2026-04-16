package com.certalert.tlscert.security;

import com.certalert.tlscert.entity.UserAccount;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class SecurityUserDetails implements UserDetails {
    private final UUID id;
    private final String username;
    private final String password;
    private final UUID groupId;
    private final String groupName;
    private final UserAccount.Role role;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUserDetails(UserAccount user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.groupId = user.getGroup().getId();
        this.groupName = user.getGroup().getName();
        this.role = user.getRole();
        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()),
                new SimpleGrantedAuthority("GROUP_" + user.getGroup().getId())
        );
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
