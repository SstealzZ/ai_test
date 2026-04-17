package com.example.certmanager.exception;

/**
 * Exception thrown when certificate parsing fails.
 */
public class CertificateParseException extends CertificateException {

    public CertificateParseException(String message) {
        super(message);
    }

    public CertificateParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CertificateParseException invalidFormat(String reason) {
        return new CertificateParseException("Invalid certificate format: " + reason);
    }

    public static CertificateParseException parsingFailed(String filename, String reason) {
        return new CertificateParseException("Failed to parse certificate " + filename + ": " + reason);
    }

    public static CertificateParseException missingRequiredField(String field) {
        return new CertificateParseException("Missing required certificate field: " + field);
    }
}
