package com.certalert.service;

import com.certalert.model.Certificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;

@Service
@Slf4j
public class CertificateParserService {

    public ParsedCertificate parseFromBytes(byte[] certBytes) {
        try (InputStream is = new ByteArrayInputStream(certBytes)) {
            X509Certificate x509 = parseX509(is);
            return mapToParsed(x509, new String(certBytes));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse certificate file: " + e.getMessage(), e);
        }
    }

    public ParsedCertificate parseFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 443;

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustAllManager()}, null);

            SSLSocketFactory factory = sslContext.getSocketFactory();
            try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
                socket.startHandshake();
                java.security.cert.Certificate[] certs = socket.getSession().getPeerCertificates();
                if (certs.length == 0) {
                    throw new IllegalStateException("No certificates found for " + url);
                }
                X509Certificate x509 = (X509Certificate) certs[0];
                String pem = encodeToPem(x509);
                return mapToParsed(x509, pem);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to fetch certificate from " + url + ": " + e.getMessage(), e);
        }
    }

    private X509Certificate parseX509(InputStream is) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(is);
    }

    private ParsedCertificate mapToParsed(X509Certificate x509, String pemData) {
        return ParsedCertificate.builder()
                .subject(x509.getSubjectX500Principal().getName())
                .issuer(x509.getIssuerX500Principal().getName())
                .serialNumber(x509.getSerialNumber().toString(16))
                .notBefore(x509.getNotBefore().toInstant())
                .notAfter(x509.getNotAfter().toInstant())
                .pemData(pemData)
                .build();
    }

    private String encodeToPem(X509Certificate cert) throws Exception {
        Base64.Encoder encoder = Base64.getMimeEncoder(64, "\n".getBytes());
        return "-----BEGIN CERTIFICATE-----\n" +
                new String(encoder.encode(cert.getEncoded())) +
                "\n-----END CERTIFICATE-----\n";
    }

    private static class X509TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }
    }

    @lombok.Builder
    @lombok.Getter @lombok.Setter @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ParsedCertificate {
        private String subject;
        private String issuer;
        private String serialNumber;
        private Instant notBefore;
        private Instant notAfter;
        private String pemData;
    }
}