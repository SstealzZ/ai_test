package com.example.certmanager.exception;

/**
 * Exception thrown when a certificate is not found.
 */
public class CertificateNotFoundException extends CertificateException {

    public CertificateNotFoundException(String message) {
        super(message);
    }

    public CertificateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CertificateNotFoundException notFound(Long id) {
        return new CertificateNotFoundException("Certificate not found with id: " + id);
    }

    public static CertificateNotFoundException notFoundByFingerprint(String fingerprint) {
        return new CertificateNotFoundException("Certificate not found with fingerprint: " + fingerprint);
    }

    public static CertificateNotFoundException notFoundByDomain(String domain) {
        return new CertificateNotFoundException("Certificate not found for domain: " + domain);
    }
}
