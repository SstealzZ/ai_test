package com.example.certmanager.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CertificateSecurityEvaluator {

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                    grantedAuthority.getAuthority().equals("ROLE_" + role) ||
                    grantedAuthority.getAuthority().equals(role)
                );
    }

    public boolean isOwner(Long userId) {
        if (userId == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return userId.equals(customUserDetails.getUserId());
        }

        String username = authentication.getName();
        return username != null && username.equals(userId.toString());
    }

    public boolean isGroupMember(Long groupId) {
        if (groupId == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return hasRole("ADMIN");
    }

    public boolean canManageCertificate(Long certificateId, Long groupId) {
        return hasRole("ADMIN") || isGroupMember(groupId);
    }
}
