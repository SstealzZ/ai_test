package com.certwatcher.service;

import com.certwatcher.domain.AppUser;
import com.certwatcher.domain.Certificate;
import com.certwatcher.domain.CertGroup;
import com.certwatcher.domain.UserRole;
import com.certwatcher.dto.CertificateResponse;
import com.certwatcher.dto.UpdateThresholdRequest;
import com.certwatcher.repository.CertGroupRepository;
import com.certwatcher.repository.CertificateRepository;
import com.certwatcher.security.CurrentUserResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class CertificateService {

    private final CertificateRepository certRepo;
    private final CertGroupRepository groupRepo;
    private final CurrentUserResolver currentUser;

    public CertificateService(CertificateRepository certRepo,
                               CertGroupRepository groupRepo,
                               CurrentUserResolver currentUser) {
        this.certRepo = certRepo;
        this.groupRepo = groupRepo;
        this.currentUser = currentUser;
    }

    public List<CertificateResponse> listForCurrentUser() {
        AppUser user = currentUser.resolve();
        return certRepo.findByGroupOrderByNotAfterDesc(user.getGroup())
                .stream()
                .map(CertificateResponse::from)
                .toList();
    }

    public CertificateResponse addFromFile(String alias, MultipartFile file) {
        AppUser user = requireAdmin();
        try {
            X509Certificate x509 = parseCertificate(file.getInputStream());
            Certificate cert = buildCertificate(alias, x509, "FILE_UPLOAD", user);
            return CertificateResponse.from(certRepo.save(cert));
        } catch (IOException | CertificateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid certificate file: " + e.getMessage());
        }
    }

    public CertificateResponse addFromUrl(String alias, String hostname, int port) {
        AppUser user = requireAdmin();
        X509Certificate x509 = fetchCertificateFromHost(hostname, port);
        String source = "URL:" + hostname + ":" + port;
        Certificate cert = buildCertificate(alias, x509, source, user);
        return CertificateResponse.from(certRepo.save(cert));
    }

    public void updateGroupSettings(UpdateThresholdRequest request) {
        AppUser user = requireAdmin();
        CertGroup group = user.getGroup();
        group.setAlertThresholdDays(request.alertThresholdDays());
        if (request.webhookUrl() != null) {
            group.setWebhookUrl(request.webhookUrl());
        }
        groupRepo.save(group);
    }

    @Transactional(readOnly = true)
    public CertGroup groupSettings() {
        return currentUser.resolve().getGroup();
    }

    private AppUser requireAdmin() {
        AppUser user = currentUser.resolve();
        if (user.getRole() != UserRole.CERT_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only CERT_ADMIN can perform this action");
        }
        return user;
    }

    private X509Certificate parseCertificate(InputStream stream) throws CertificateException, IOException {
        byte[] bytes = stream.readAllBytes();
        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        try {
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            throw new CertificateException("Could not parse as PEM or DER: " + e.getMessage());
        }
    }

    /**
     * Opens a TLS handshake to the target host and extracts the leaf certificate
     * from the presented chain without trusting or storing any sensitive key material.
     */
    private X509Certificate fetchCertificateFromHost(String hostname, int port) {
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try (SSLSocket socket = (SSLSocket) factory.createSocket(hostname, port)) {
                socket.startHandshake();
                var chain = socket.getSession().getPeerCertificates();
                if (chain == null || chain.length == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No certificate presented by " + hostname);
                }
                return (X509Certificate) chain[0];
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not connect to " + hostname + ":" + port + " — " + e.getMessage());
        }
    }

    private Certificate buildCertificate(String alias, X509Certificate x509, String source, AppUser user) {
        Certificate cert = new Certificate();
        cert.setAlias(alias);
        cert.setSubjectDn(x509.getSubjectX500Principal().getName());
        cert.setIssuerDn(x509.getIssuerX500Principal().getName());
        cert.setNotBefore(x509.getNotBefore().toInstant());
        cert.setNotAfter(x509.getNotAfter().toInstant());
        cert.setSerialNumber(x509.getSerialNumber().toString(16));
        cert.setSource(source);
        cert.setGroup(user.getGroup());
        cert.setAddedByUsername(user.getUsername());
        cert.setAddedAt(Instant.now());
        return cert;
    }
}
