package com.certalert.service;

import com.certalert.dto.CertificateResponse;
import com.certalert.model.AppGroup;
import com.certalert.model.AppUser;
import com.certalert.model.Certificate;
import com.certalert.repository.AppGroupRepository;
import com.certalert.repository.AppUserRepository;
import com.certalert.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final AppUserRepository appUserRepository;
    private final AppGroupRepository appGroupRepository;
    private final CertificateParserService certificateParserService;

    @Transactional
    public CertificateResponse addCertificateFromFile(MultipartFile file, String username) {
        AppUser user = getUser(username);
        ensureCanManage(user);

        byte[] certBytes;
        try {
            certBytes = file.getBytes();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read certificate file", e);
        }

        CertificateParserService.ParsedCertificate parsed = certificateParserService.parseFromBytes(certBytes);

        return saveCertificate(parsed, user, file.getOriginalFilename(), null);
    }

    @Transactional
    public CertificateResponse addCertificateFromUrl(String url, String username) {
        AppUser user = getUser(username);
        ensureCanManage(user);

        CertificateParserService.ParsedCertificate parsed = certificateParserService.parseFromUrl(url);

        return saveCertificate(parsed, user, null, url);
    }

    public List<CertificateResponse> listCertificates(String username) {
        AppUser user = getUser(username);
        return certificateRepository.findByGroupIdOrderByNotAfterDesc(user.getGroup().getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CertificateResponse getCertificate(Long id, String username) {
        AppUser user = getUser(username);
        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found: " + id));

        if (!cert.getGroup().getId().equals(user.getGroup().getId())) {
            throw new SecurityException("Access denied: certificate belongs to a different group");
        }

        return toResponse(cert);
    }

    @Transactional
    public void deleteCertificate(Long id, String username) {
        AppUser user = getUser(username);
        ensureCanManage(user);

        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found: " + id));

        if (!cert.getGroup().getId().equals(user.getGroup().getId())) {
            throw new SecurityException("Access denied: certificate belongs to a different group");
        }

        certificateRepository.delete(cert);
        log.info("Certificate deleted: id={}, subject={}, deletedBy={}", id, cert.getSubject(), username);
    }

    public List<CertificateResponse> getExpiringCertificates(String username) {
        AppUser user = getUser(username);
        AppGroup group = user.getGroup();
        Instant now = Instant.now();

        return certificateRepository.findByGroupIdOrderByNotAfterDesc(group.getId())
                .stream()
                .filter(cert -> cert.daysUntilExpiry() <= 30)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CertificateResponse saveCertificate(CertificateParserService.ParsedCertificate parsed,
                                                 AppUser user, String originalFilename, String sourceUrl) {
        if (certificateRepository.existsBySerialNumber(parsed.getSerialNumber())) {
            throw new IllegalArgumentException("Certificate with serial number " + parsed.getSerialNumber() + " already exists");
        }

        Certificate cert = Certificate.builder()
                .subject(parsed.getSubject())
                .issuer(parsed.getIssuer())
                .serialNumber(parsed.getSerialNumber())
                .notBefore(parsed.getNotBefore())
                .notAfter(parsed.getNotAfter())
                .pemData(parsed.getPemData())
                .sourceUrl(sourceUrl)
                .originalFilename(originalFilename)
                .group(user.getGroup())
                .addedBy(user.getUsername())
                .build();

        cert = certificateRepository.save(cert);
        log.info("Certificate added: id={}, subject={}, group={}", cert.getId(), cert.getSubject(), user.getGroup().getName());
        return toResponse(cert);
    }

    private CertificateResponse toResponse(Certificate cert) {
        long daysLeft = cert.daysUntilExpiry();
        String status;
        if (cert.isExpired()) {
            status = "EXPIRED";
        } else if (daysLeft <= 7) {
            status = "CRITICAL";
        } else if (daysLeft <= 30) {
            status = "WARNING";
        } else {
            status = "VALID";
        }

        return CertificateResponse.builder()
                .id(cert.getId())
                .subject(cert.getSubject())
                .issuer(cert.getIssuer())
                .serialNumber(cert.getSerialNumber())
                .notBefore(cert.getNotBefore())
                .notAfter(cert.getNotAfter())
                .sourceUrl(cert.getSourceUrl())
                .originalFilename(cert.getOriginalFilename())
                .groupName(cert.getGroup().getName())
                .addedBy(cert.getAddedBy())
                .createdAt(cert.getCreatedAt())
                .daysUntilExpiry(daysLeft)
                .expired(cert.isExpired())
                .status(status)
                .build();
    }

    private AppUser getUser(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    private void ensureCanManage(AppUser user) {
        if (user.getRole() != AppUser.Role.CERT_MANAGER) {
            throw new SecurityException("Only CERT_MANAGER role can add certificates");
        }
    }

    public Long getUserGroupId(String username) {
        return getUser(username).getGroup().getId();
    }
}