package com.example.certmanager.service;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Service for parsing and validating X.509 certificates using Bouncy Castle.
 */
@Service
public class CertificateParserService {

    private static final Logger log = LoggerFactory.getLogger(CertificateParserService.class);

    /**
     * Parse X.509 certificate from input stream.
     *
     * @param inputStream the input stream containing PEM or DER encoded certificate
     * @return the parsed X.509 certificate
     * @throws com.example.certmanager.exception.CertificateException if parsing fails
     */
    public X509Certificate parseFromInputStream(InputStream inputStream) throws com.example.certmanager.exception.CertificateException {
        try {
            // Try PEM format first
            return parsePemCertificate(inputStream);
        } catch (Exception e) {
            log.debug("PEM parsing failed, trying DER format", e);
            // Reset stream and try DER format
            try {
                inputStream.reset();
                return parseDerCertificate(inputStream);
            } catch (IOException ex) {
                throw com.example.certmanager.exception.CertificateException.invalidFormat("Unable to parse certificate: " + ex.getMessage());
            }
        }
    }

    /**
     * Parse X.509 certificate chain from URL.
     *
     * @param url the URL to fetch the certificate from
     * @return list of certificates in the chain (leaf certificate first)
     * @throws com.example.certmanager.exception.CertificateException if fetching or parsing fails
     */
    public List<X509Certificate> parseFromUrl(String url) throws com.example.certmanager.exception.CertificateException {
        try {
            log.info("Fetching certificate from URL: {}", url);

            URL certificateUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) certificateUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw com.example.certmanager.exception.CertificateException.fetchFailed(url, "HTTP " + responseCode);
            }

            try (InputStream inputStream = connection.getInputStream()) {
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                return parseCertificateChain(content);
            }
        } catch (IOException e) {
            log.error("Failed to fetch certificate from URL: {}", url, e);
            throw com.example.certmanager.exception.CertificateException.fetchFailed(url, e.getMessage());
        }
    }

    /**
     * Extract metadata from X.509 certificate.
     *
     * @param cert the X.509 certificate
     * @return map containing certificate metadata
     */
    public Map<String, Object> extractMetadata(X509Certificate cert) {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("commonName", extractCommonName(cert));
        metadata.put("subject", cert.getSubjectX500Principal().getName());
        metadata.put("issuer", cert.getIssuerX500Principal().getName());
        metadata.put("serialNumber", cert.getSerialNumber().toString(16));
        metadata.put("notBefore", Instant.ofEpochMilli(cert.getNotBefore().getTime()));
        metadata.put("notAfter", Instant.ofEpochMilli(cert.getNotAfter().getTime()));
        metadata.put("fingerprintSha256", calculateFingerprint(cert));
        metadata.put("version", cert.getVersion());
        metadata.put("signatureAlgorithm", cert.getSigAlgName());
        metadata.put("keySize", getKeySize(cert));
        metadata.put("subjectAlternativeNames", extractSubjectAlternativeNames(cert));

        return metadata;
    }

    /**
     * Validate certificate - check if it's currently valid.
     *
     * @param cert the X.509 certificate to validate
     * @throws com.example.certmanager.exception.CertificateException if certificate is invalid or expired
     */
    public void validateCertificate(X509Certificate cert) throws com.example.certmanager.exception.CertificateException {
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
            throw com.example.certmanager.exception.CertificateException.expired(extractCommonName(cert));
        } catch (CertificateNotYetValidException e) {
            throw com.example.certmanager.exception.CertificateException.invalidFormat("Certificate is not yet valid");
        }
    }

    /**
     * Validate certificate with a specific threshold.
     *
     * @param cert the X.509 certificate to validate
     * @param thresholdDays number of days before expiry to consider as expiring soon
     * @return true if certificate is valid, false if expiring soon or expired
     */
    public boolean validateCertificateWithThreshold(X509Certificate cert, int thresholdDays) {
        try {
            cert.checkValidity();
            Instant expiryDate = Instant.ofEpochMilli(cert.getNotAfter().getTime());
            Instant thresholdDate = Instant.now().plus(thresholdDays, java.time.temporal.ChronoUnit.DAYS);
            return expiryDate.isAfter(thresholdDate);
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            return false;
        }
    }

    private X509Certificate parsePemCertificate(InputStream inputStream) throws Exception {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             PEMParser pemParser = new PEMParser(reader)) {

            Object pemObject = pemParser.readObject();
            if (pemObject == null) {
                throw com.example.certmanager.exception.CertificateException.invalidFormat("Empty PEM content");
            }

            if (pemObject instanceof X509CertificateHolder) {
                X509CertificateHolder certHolder = (X509CertificateHolder) pemObject;
                return new JcaX509CertificateConverter()
                        .setProvider("BC")
                        .getCertificate(certHolder);
            } else if (pemObject instanceof java.security.cert.Certificate) {
                return (X509Certificate) pemObject;
            } else {
                throw com.example.certmanager.exception.CertificateException.invalidFormat("Unexpected PEM object type: " + pemObject.getClass().getName());
            }
        } catch (java.security.cert.CertificateException e) {
            throw com.example.certmanager.exception.CertificateException.invalidFormat("Failed to create operator: " + e.getMessage());
        } catch (IOException e) {
            throw com.example.certmanager.exception.CertificateException.invalidFormat("Failed to read PEM: " + e.getMessage());
        }
    }

    private X509Certificate parseDerCertificate(InputStream inputStream) throws com.example.certmanager.exception.CertificateException {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");
            Certificate cert = factory.generateCertificate(inputStream);
            return (X509Certificate) cert;
        } catch (CertificateException | NoSuchProviderException e) {
            throw com.example.certmanager.exception.CertificateException.invalidFormat("Failed to parse DER certificate: " + e.getMessage());
        }
    }

    private List<X509Certificate> parseCertificateChain(String content) throws com.example.certmanager.exception.CertificateException {
        List<X509Certificate> certificates = new ArrayList<>();

        try (StringReader reader = new StringReader(content);
             PEMParser pemParser = new PEMParser(reader)) {

            Object pemObject;
            while ((pemObject = pemParser.readObject()) != null) {
                if (pemObject instanceof X509CertificateHolder) {
                    X509CertificateHolder certHolder = (X509CertificateHolder) pemObject;
                    X509Certificate cert = new JcaX509CertificateConverter()
                            .setProvider("BC")
                            .getCertificate(certHolder);
                    certificates.add(cert);
                }
            }
        } catch (IOException | java.security.cert.CertificateException e) {
            throw com.example.certmanager.exception.CertificateException.invalidFormat("Failed to parse certificate chain: " + e.getMessage());
        }

        if (certificates.isEmpty()) {
            throw com.example.certmanager.exception.CertificateException.invalidFormat("No certificates found in chain");
        }

        return certificates;
    }

    private String extractCommonName(X509Certificate cert) {
        String subject = cert.getSubjectX500Principal().getName();
        return extractDNField(subject, "CN");
    }

    private String extractDNField(String dn, String field) {
        if (dn == null || field == null) {
            return null;
        }

        String[] parts = dn.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith(field + "=")) {
                return trimmed.substring(field.length() + 1);
            }
        }
        return null;
    }

    private String calculateFingerprint(X509Certificate cert) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
            byte[] encoded = cert.getEncoded();
            byte[] hash = digest.digest(encoded);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | CertificateEncodingException e) {
            log.error("Failed to calculate fingerprint", e);
            return null;
        }
    }

    private int getKeySize(X509Certificate cert) {
        try {
            PublicKey publicKey = cert.getPublicKey();
            if (publicKey instanceof java.security.interfaces.RSAPublicKey) {
                return ((java.security.interfaces.RSAPublicKey) publicKey).getModulus().bitLength();
            } else if (publicKey instanceof java.security.interfaces.ECPublicKey) {
                return ((java.security.interfaces.ECPublicKey) publicKey).getParams().getCurve().getField().getFieldSize();
            }
        } catch (Exception e) {
            log.debug("Could not determine key size", e);
        }
        return -1;
    }

    private List<String> extractSubjectAlternativeNames(X509Certificate cert) {
        List<String> sanList = new ArrayList<>();
        try {
            Collection<List<?>> altNames = cert.getSubjectAlternativeNames();
            if (altNames != null) {
                for (List<?> altName : altNames) {
                    if (altName.size() >= 2) {
                        Object value = altName.get(1);
                        if (value instanceof String) {
                            sanList.add((String) value);
                        }
                    }
                }
            }
        } catch (CertificateParsingException e) {
            log.debug("Could not parse subject alternative names", e);
        }
        return sanList;
    }
}
