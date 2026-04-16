package com.example.certmanager.exception;

/**
 * Exception thrown when a certificate has expired.
 */
public class CertificateExpiredException extends CertificateException {

    public CertificateExpiredException(String message) {
        super(message);
    }

    public CertificateExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CertificateExpiredException expired(String commonName) {
        return new CertificateExpiredException("Certificate has expired: " + commonName);
    }

    public static CertificateExpiredException expired(Long id, String commonName) {
        return new CertificateExpiredException("Certificate " + id + " has expired: " + commonName);
    }
}
