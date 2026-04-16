package com.demo.tlsalert.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public DemoUserPrincipal getRequiredUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("No authenticated user found");
        }
        if (!(authentication.getPrincipal() instanceof DemoUserPrincipal principal)) {
            throw new IllegalStateException("Unsupported principal type");
        }
        return principal;
    }

    public String currentGroup() {
        return getRequiredUser().getGroupName();
    }

    public String currentUsername() {
        return getRequiredUser().getUsername();
    }
}
