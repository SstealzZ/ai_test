package com.example.certmanager.exception;

/**
 * Exception thrown when a group is not found.
 */
public class GroupNotFoundException extends RuntimeException {

    public GroupNotFoundException(String message) {
        super(message);
    }

    public GroupNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static GroupNotFoundException notFound(Long id) {
        return new GroupNotFoundException("Group not found with id: " + id);
    }

    public static GroupNotFoundException notFoundByName(String name) {
        return new GroupNotFoundException("Group not found with name: " + name);
    }

    public static GroupNotFoundException userNotInGroup(Long groupId, Long userId) {
        return new GroupNotFoundException("User " + userId + " is not a member of group " + groupId);
    }
}
