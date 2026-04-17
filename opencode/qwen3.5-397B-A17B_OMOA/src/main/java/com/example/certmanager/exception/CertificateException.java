package com.example.certmanager.exception;

/**
 * Exception thrown when certificate-related operations fail.
 */
public class CertificateException extends RuntimeException {

    public CertificateException(String message) {
        super(message);
    }

    public CertificateException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CertificateException notFound(Long id) {
        return new CertificateException("Certificate not found with id: " + id);
    }

    public static CertificateException notFoundByFingerprint(String fingerprint) {
        return new CertificateException("Certificate not found with fingerprint: " + fingerprint);
    }

    public static CertificateException alreadyExists(String fingerprint) {
        return new CertificateException("Certificate already exists with fingerprint: " + fingerprint);
    }

    public static CertificateException invalidFormat(String reason) {
        return new CertificateException("Invalid certificate format: " + reason);
    }

    public static CertificateException expired(String commonName) {
        return new CertificateException("Certificate has expired: " + commonName);
    }

    public static CertificateException fetchFailed(String url, String reason) {
        return new CertificateException("Failed to fetch certificate from URL " + url + ": " + reason);
    }

    public static CertificateException accessDenied(Long certificateId, Long userId) {
        return new CertificateException("Access denied to certificate " + certificateId + " for user " + userId);
    }

    public static CertificateException refreshFailed(Long id, String reason) {
        return new CertificateException("Failed to refresh certificate " + id + ": " + reason);
    }
}
