package com.certalert.tlscert.service;

import com.certalert.tlscert.entity.Certificate;
import com.certalert.tlscert.entity.UserAccount;
import com.certalert.tlscert.repository.CertificateRepository;
import com.certalert.tlscert.security.CurrentUser;
import com.certalert.tlscert.security.SecurityUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserAccountService userAccountService;

    public CertificateService(CertificateRepository certificateRepository, UserAccountService userAccountService) {
        this.certificateRepository = certificateRepository;
        this.userAccountService = userAccountService;
    }

    @Transactional
    public Certificate uploadFromFile(MultipartFile file) throws CertificateException, IOException {
        SecurityUserDetails currentUser = CurrentUser.get();
        UserAccount user = userAccountService.findById(currentUser.getId());

        X509Certificate x509 = parseCertificate(file.getBytes());
        Certificate cert = mapToEntity(x509, user, Certificate.SourceType.UPLOAD, null);
        cert.setRawPem(encodePem(file.getBytes()));
        return certificateRepository.save(cert);
    }

    @Transactional
    public Certificate fetchFromUrl(String urlString) throws Exception {
        SecurityUserDetails currentUser = CurrentUser.get();
        UserAccount user = userAccountService.findById(currentUser.getId());

        URL url = new URL(urlString);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.connect();
        java.security.cert.Certificate[] chain = conn.getServerCertificates();
        conn.disconnect();

        if (chain.length == 0) {
            throw new CertificateException("No certificates returned from URL");
        }

        X509Certificate x509 = (X509Certificate) chain[0];
        Certificate cert = mapToEntity(x509, user, Certificate.SourceType.URL, urlString);
        cert.setRawPem(encodePem(x509.getEncoded()));
        return certificateRepository.save(cert);
    }

    @Transactional(readOnly = true)
    public List<Certificate> listForCurrentUser() {
        SecurityUserDetails currentUser = CurrentUser.get();
        return certificateRepository.findByGroupIdOrderByValidUntilAsc(currentUser.getGroupId());
    }

    @Transactional(readOnly = true)
    public List<Certificate> listExpiringForCurrentUser(int thresholdDays) {
        SecurityUserDetails currentUser = CurrentUser.get();
        Instant threshold = Instant.now().plus(thresholdDays, ChronoUnit.DAYS);
        return certificateRepository.findByGroupIdAndValidUntilBeforeOrderByValidUntilAsc(currentUser.getGroupId(), threshold);
    }

    @Transactional(readOnly = true)
    public Optional<Certificate> findById(java.util.UUID id) {
        return certificateRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Certificate> findAllExpiringWithin(int thresholdDays) {
        Instant threshold = Instant.now().plus(thresholdDays, ChronoUnit.DAYS);
        return certificateRepository.findByValidUntilBeforeOrderByValidUntilAsc(threshold);
    }

    private X509Certificate parseCertificate(byte[] bytes) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
    }

    private Certificate mapToEntity(X509Certificate x509, UserAccount user, Certificate.SourceType sourceType, String sourceUrl) {
        return Certificate.builder()
                .group(user.getGroup())
                .uploadedBy(user)
                .subjectDn(x509.getSubjectX500Principal().getName())
                .issuerDn(x509.getIssuerX500Principal().getName())
                .serialNumber(x509.getSerialNumber().toString(16))
                .sha256Fingerprint(sha256Fingerprint(x509))
                .validFrom(x509.getNotBefore().toInstant())
                .validUntil(x509.getNotAfter().toInstant())
                .sourceType(sourceType)
                .sourceUrl(sourceUrl)
                .build();
    }

    private String sha256Fingerprint(X509Certificate cert) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cert.getEncoded());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String encodePem(byte[] encoded) {
        String base64 = Base64.getEncoder().encodeToString(encoded);
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN CERTIFICATE-----\n");
        int idx = 0;
        while (idx < base64.length()) {
            pem.append(base64, idx, Math.min(idx + 64, base64.length())).append("\n");
            idx += 64;
        }
        pem.append("-----END CERTIFICATE-----\n");
        return pem.toString();
    }
}
