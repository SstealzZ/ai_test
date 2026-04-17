package com.example.certmanager.exception;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ValidationException invalidThreshold(Integer thresholdDays) {
        return new ValidationException("Invalid threshold days: " + thresholdDays);
    }

    public static ValidationException invalidCertificate(String reason) {
        return new ValidationException("Invalid certificate: " + reason);
    }

    public static ValidationException invalidUrl(String url) {
        return new ValidationException("Invalid URL: " + url);
    }

    public static ValidationException invalidGroupName(String name) {
        return new ValidationException("Invalid group name: " + name);
    }

    public static ValidationException duplicateGroupName(String name) {
        return new ValidationException("Group with name already exists: " + name);
    }
}
