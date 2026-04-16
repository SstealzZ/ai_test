package com.example.certmanager.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        super(resourceType + " not found with id: " + id);
    }

    public static ResourceNotFoundException groupNotFound(Long id) {
        return new ResourceNotFoundException("Group", id);
    }

    public static ResourceNotFoundException groupNotFoundByName(String name) {
        return new ResourceNotFoundException("Group not found with name: " + name);
    }

    public static ResourceNotFoundException userNotFound(Long id) {
        return new ResourceNotFoundException("User", id);
    }

    public static ResourceNotFoundException userNotFoundByEmail(String email) {
        return new ResourceNotFoundException("User not found with email: " + email);
    }

    public static ResourceNotFoundException certificateNotFound(Long id) {
        return new ResourceNotFoundException("Certificate", id);
    }

    public static ResourceNotFoundException alertNotFound(Long id) {
        return new ResourceNotFoundException("Alert", id);
    }

    public static ResourceNotFoundException memberNotFound(Long groupId, Long userId) {
        return new ResourceNotFoundException("Group member not found for group " + groupId + " and user " + userId);
    }
}
