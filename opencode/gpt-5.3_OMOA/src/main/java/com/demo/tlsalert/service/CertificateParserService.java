package com.demo.tlsalert.service;

import com.demo.tlsalert.common.BadRequestException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

@Service
public class CertificateParserService {

    public X509Certificate parseFirstX509(byte[] rawBytes) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certificates = factory.generateCertificates(new ByteArrayInputStream(rawBytes));
            if (certificates.isEmpty()) {
                throw new BadRequestException("No certificate found in provided content");
            }
            Certificate first = certificates.iterator().next();
            if (!(first instanceof X509Certificate x509Certificate)) {
                throw new BadRequestException("Provided file is not an X.509 certificate");
            }
            return x509Certificate;
        } catch (CertificateException ex) {
            throw new BadRequestException("Unable to parse certificate: " + ex.getMessage());
        }
    }

    public String sha256Fingerprint(X509Certificate certificate) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(certificate.getEncoded());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | CertificateException ex) {
            throw new IllegalStateException("Unable to compute certificate fingerprint", ex);
        }
    }
}
