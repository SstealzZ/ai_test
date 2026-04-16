package com.demo.tlsalert.service;

import com.demo.tlsalert.api.dto.CertificateResponse;
import com.demo.tlsalert.common.ConflictException;
import com.demo.tlsalert.domain.CertificateEntity;
import com.demo.tlsalert.domain.SourceType;
import com.demo.tlsalert.repository.CertificateRepository;
import com.demo.tlsalert.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CurrentUserService currentUserService;
    private final CertificateParserService certificateParserService;

    public CertificateService(
        CertificateRepository certificateRepository,
        CurrentUserService currentUserService,
        CertificateParserService certificateParserService
    ) {
        this.certificateRepository = certificateRepository;
        this.currentUserService = currentUserService;
        this.certificateParserService = certificateParserService;
    }

    @Transactional
    public CertificateResponse createFromX509(X509Certificate certificate, SourceType sourceType, String sourceValue) {
        String group = currentUserService.currentGroup();
        String fingerprint = certificateParserService.sha256Fingerprint(certificate);

        if (certificateRepository.existsByGroupNameAndFingerprintSha256(group, fingerprint)) {
            throw new ConflictException("Certificate already exists for your group");
        }

        CertificateEntity entity = new CertificateEntity();
        entity.setGroupName(group);
        entity.setSourceType(sourceType);
        entity.setSourceValue(sourceValue);
        entity.setSubjectDn(certificate.getSubjectX500Principal().getName());
        entity.setIssuerDn(certificate.getIssuerX500Principal().getName());
        entity.setSerialNumber(certificate.getSerialNumber().toString(16));
        entity.setFingerprintSha256(fingerprint);
        entity.setNotBefore(certificate.getNotBefore().toInstant());
        entity.setNotAfter(certificate.getNotAfter().toInstant());
        entity.setCreatedBy(currentUserService.currentUsername());
        entity.setCreatedAt(Instant.now());

        CertificateEntity saved = certificateRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> listCurrentGroupCertificates() {
        String group = currentUserService.currentGroup();
        return certificateRepository.findByGroupNameOrderByNotAfterDesc(group)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public CertificateResponse toResponse(CertificateEntity entity) {
        long daysRemaining = Duration.between(Instant.now(), entity.getNotAfter()).toDays();
        return new CertificateResponse(
            entity.getId(),
            entity.getSourceType(),
            entity.getSourceValue(),
            entity.getSubjectDn(),
            entity.getIssuerDn(),
            entity.getSerialNumber(),
            entity.getFingerprintSha256(),
            entity.getNotBefore(),
            entity.getNotAfter(),
            daysRemaining
        );
    }
}
