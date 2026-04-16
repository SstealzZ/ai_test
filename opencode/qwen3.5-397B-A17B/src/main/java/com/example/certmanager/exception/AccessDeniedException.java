package com.example.certmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when access control checks fail.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AccessDeniedException groupAccessDenied(Long groupId, Long userId) {
        return new AccessDeniedException("User " + userId + " does not have access to group " + groupId);
    }

    public static AccessDeniedException certificateAccessDenied(Long certificateId, Long userId) {
        return new AccessDeniedException("User " + userId + " does not have access to certificate " + certificateId);
    }

    public static AccessDeniedException insufficientRole(String requiredRole, Long userId) {
        return new AccessDeniedException("User " + userId + " does not have required role: " + requiredRole);
    }

    public static AccessDeniedException notGroupMember(Long groupId, Long userId) {
        return new AccessDeniedException("User " + userId + " is not a member of group " + groupId);
    }

    public static AccessDeniedException notGroupOwner(Long groupId, Long userId) {
        return new AccessDeniedException("User " + userId + " is not an owner of group " + groupId);
    }
}
