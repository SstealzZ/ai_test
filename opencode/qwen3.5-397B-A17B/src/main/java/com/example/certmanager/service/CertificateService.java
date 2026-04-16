package com.example.certmanager.service;

import com.example.certmanager.dto.request.CertificateRequest;
import com.example.certmanager.dto.request.CertificateUrlRequest;
import com.example.certmanager.dto.request.ThresholdUpdateRequest;
import com.example.certmanager.dto.response.CertificateResponse;
import com.example.certmanager.dto.response.PagedResponse;
import com.example.certmanager.entity.Certificate;
import com.example.certmanager.entity.GroupMember;
import com.example.certmanager.entity.Group;
import com.example.certmanager.entity.User;
import com.example.certmanager.exception.AccessDeniedException;
import com.example.certmanager.exception.CertificateException;
import com.example.certmanager.exception.ResourceNotFoundException;
import com.example.certmanager.repository.CertificateRepository;
import com.example.certmanager.repository.GroupRepository;
import com.example.certmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Service for managing certificates.
 */
@Service
@Transactional
public class CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateService.class);

    private final CertificateRepository certificateRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final CertificateParserService parserService;
    private final GroupService groupService;
    private final AlertService alertService;
    private final AuditService auditService;

    public CertificateService(CertificateRepository certificateRepository,
                              GroupRepository groupRepository,
                              UserRepository userRepository,
                              CertificateParserService parserService,
                              GroupService groupService,
                              AlertService alertService,
                              AuditService auditService) {
        this.certificateRepository = certificateRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.parserService = parserService;
        this.groupService = groupService;
        this.alertService = alertService;
        this.auditService = auditService;
    }

    /**
     * Add certificate from uploaded file.
     *
     * @param file the certificate file
     * @param groupId the group ID
     * @param thresholdDays the threshold days for expiry warning
     * @param userId the user ID adding the certificate
     * @return the created certificate
     */
    public Certificate addCertificateFromFile(MultipartFile file, Long groupId, Integer thresholdDays, Long userId) {
        log.info("Adding certificate from file for group {}", groupId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));

        // Validate group access
        if (groupId != null) {
            validateGroupAccess(groupId, userId);
        }

        X509Certificate cert;
        try {
            cert = parserService.parseFromInputStream(file.getInputStream());
        } catch (IOException e) {
            throw CertificateException.invalidFormat("Failed to read certificate file: " + e.getMessage());
        }

        return saveCertificate(cert, null, groupId, thresholdDays, user, "FILE_UPLOAD");
    }

    /**
     * Add certificate from URL.
     *
     * @param url the certificate URL
     * @param groupId the group ID
     * @param thresholdDays the threshold days for expiry warning
     * @param userId the user ID adding the certificate
     * @return the created certificate
     */
    public Certificate addCertificateFromUrl(String url, Long groupId, Integer thresholdDays, Long userId) {
        log.info("Adding certificate from URL: {} for group {}", url, groupId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));

        // Validate group access
        if (groupId != null) {
            validateGroupAccess(groupId, userId);
        }

        List<X509Certificate> certs = parserService.parseFromUrl(url);
        if (certs.isEmpty()) {
            throw CertificateException.invalidFormat("No certificates found at URL");
        }

        return saveCertificate(certs.get(0), url, groupId, thresholdDays, user, "URL_FETCH");
    }

    /**
     * Get certificate by ID with group access check.
     *
     * @param id the certificate ID
     * @param userId the user ID requesting access
     * @return the certificate
     */
    @Transactional(readOnly = true)
    public CertificateResponse getCertificateById(Long id, Long userId) {
        Certificate certificate = getCertificateEntityById(id, userId);
        return mapToResponse(certificate);
    }

    @Transactional(readOnly = true)
    public Certificate getCertificateEntityById(Long id, Long userId) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> CertificateException.notFound(id));

        validateCertificateAccess(id, userId);

        return certificate;
    }

    /**
     * List certificates for a group.
     *
     * @param groupId the group ID
     * @param status optional status filter
     * @param pageable pagination info
     * @param userId the user ID requesting the list
     * @return page of certificates
     */
    @Transactional(readOnly = true)
    public Page<Certificate> listCertificates(Long groupId, String status, Pageable pageable, Long userId) {
        validateGroupAccess(groupId, userId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> ResourceNotFoundException.groupNotFound(groupId));

        if (status != null && !status.isEmpty()) {
            try {
                Certificate.Status certStatus = Certificate.Status.valueOf(status.toUpperCase());
                return certificateRepository.findByGroupAndStatusSortedByExpiry(group, certStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw CertificateException.invalidFormat("Invalid status: " + status);
            }
        }

        return certificateRepository.findByGroupSortedByExpiry(group, pageable);
    }

    /**
     * Delete certificate with permission check.
     *
     * @param id the certificate ID
     * @param userId the user ID deleting the certificate
     */
    public void deleteCertificate(Long id, Long userId) {
        log.info("Deleting certificate: {}", id);

        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> CertificateException.notFound(id));

        validateCertificateAccess(id, userId);

        // Check if user has permission to delete (group OWNER or certificate creator)
        boolean hasPermission = false;
        if (certificate.getCreatedBy() != null && certificate.getCreatedBy().getId().equals(userId)) {
            hasPermission = true;
        } else if (certificate.getGroup() != null) {
            GroupMember.Role role = groupService.getUserRoleInGroup(userId, certificate.getGroup().getId());
            if (role == GroupMember.Role.OWNER || role == GroupMember.Role.ADMIN) {
                hasPermission = true;
            }
        }

        if (!hasPermission) {
            throw AccessDeniedException.certificateAccessDenied(id, userId);
        }

        certificateRepository.delete(certificate);

        log.info("Deleted certificate: {}", id);

        auditService.logAction(
                userId,
                "DELETE_CERTIFICATE",
                "CERTIFICATE",
                id,
                "Deleted certificate: " + certificate.getCommonName(),
                null
        );
    }

    /**
     * Update threshold days for a certificate.
     *
     * @param id the certificate ID
     * @param thresholdDays the new threshold days
     * @param userId the user ID making the update
     * @return the updated certificate
     */
    public Certificate updateThreshold(Long id, Integer thresholdDays, Long userId) {
        log.info("Updating threshold for certificate {}: {} days", id, thresholdDays);

        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> CertificateException.notFound(id));

        validateCertificateAccess(id, userId);

        if (thresholdDays < 1 || thresholdDays > 365) {
            throw CertificateException.invalidFormat("Threshold must be between 1 and 365 days");
        }

        Integer oldThreshold = certificate.getThresholdDays();
        certificate.setThresholdDays(thresholdDays);

        Certificate saved = certificateRepository.save(certificate);

        auditService.logAction(
                userId,
                "UPDATE_THRESHOLD",
                "CERTIFICATE",
                id,
                "Updated threshold from " + oldThreshold + " to " + thresholdDays,
                null
        );

        return saved;
    }

    /**
     * Refresh certificate from URL.
     *
     * @param id the certificate ID
     * @param userId the user ID refreshing the certificate
     * @return the refreshed certificate response
     */
    public CertificateResponse refreshCertificate(Long id, Long userId) {
        Certificate certificate = refreshCertificateEntity(id, userId);
        return mapToResponse(certificate);
    }

    public Certificate refreshCertificateEntity(Long id, Long userId) {
        log.info("Refreshing certificate: {}", id);

        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> CertificateException.notFound(id));

        validateCertificateAccess(id, userId);

        if (certificate.getSourceUrl() == null || certificate.getSourceUrl().isEmpty()) {
            throw CertificateException.refreshFailed(id, "Certificate has no source URL");
        }

        try {
            List<X509Certificate> certs = parserService.parseFromUrl(certificate.getSourceUrl());
            if (certs.isEmpty()) {
                throw CertificateException.refreshFailed(id, "No certificates found at source URL");
            }

            X509Certificate newCert = certs.get(0);
            Map<String, Object> metadata = parserService.extractMetadata(newCert);

            // Update certificate fields
            certificate.setCommonName((String) metadata.get("commonName"));
            certificate.setIssuer((String) metadata.get("issuer"));
            certificate.setSubject((String) metadata.get("subject"));
            certificate.setSerialNumber((String) metadata.get("serialNumber"));
            certificate.setFingerprintSha256((String) metadata.get("fingerprintSha256"));
            certificate.setNotBefore((Instant) metadata.get("notBefore"));
            certificate.setNotAfter((Instant) metadata.get("notAfter"));
            certificate.setLastRefreshedAt(Instant.now());

            // Update status based on expiry
            updateCertificateStatus(certificate);

            Certificate saved = certificateRepository.save(certificate);

            auditService.logAction(
                    userId,
                    "REFRESH_CERTIFICATE",
                    "CERTIFICATE",
                    id,
                    "Refreshed certificate from URL: " + certificate.getSourceUrl(),
                    null
            );

            return saved;
        } catch (Exception e) {
            throw CertificateException.refreshFailed(id, e.getMessage());
        }
    }

    /**
     * List certificates for a user with optional group and status filters.
     */
    @Transactional(readOnly = true)
    public PagedResponse<CertificateResponse> listCertificates(Long userId, Long groupId, String status, Pageable pageable) {
        Page<Certificate> page;
        if (groupId != null) {
            page = listCertificates(groupId, status, pageable, userId);
        } else {
            page = certificateRepository.findAll(pageable);
        }
        List<CertificateResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                page.isFirst()
        );
    }

    public CertificateResponse addCertificate(CertificateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        if (request.getGroupId() != null) {
            validateGroupAccess(request.getGroupId(), userId);
        }
        X509Certificate cert = parserService.parseFromInputStream(
                new ByteArrayInputStream(request.getPemContent().getBytes(StandardCharsets.UTF_8)));
        Certificate certificate = saveCertificate(cert, null, request.getGroupId(), 30, user, "PEM_UPLOAD");
        certificate.setPemCertificate(request.getPemContent());
        if (request.getPemPrivateKey() != null) {
            certificate.setPemPrivateKey(request.getPemPrivateKey());
        }
        certificate = certificateRepository.save(certificate);
        return mapToResponse(certificate);
    }

    public CertificateResponse addCertificateFromUrl(CertificateUrlRequest request, Long userId) {
        Certificate certificate = addCertificateFromUrl(request.getUrl(), request.getGroupId(), 30, userId);
        return mapToResponse(certificate);
    }

    public CertificateResponse updateThreshold(Long id, ThresholdUpdateRequest request, Long userId) {
        Certificate certificate = updateThreshold(id, request.getThresholdDays(), userId);
        return mapToResponse(certificate);
    }

    private CertificateResponse mapToResponse(Certificate certificate) {
        CertificateResponse response = new CertificateResponse();
        response.setId(certificate.getId());
        response.setCommonName(certificate.getCommonName());
        response.setIssuer(certificate.getIssuer());
        response.setSubject(certificate.getSubject());
        response.setSerialNumber(certificate.getSerialNumber());
        response.setFingerprintSha256(certificate.getFingerprintSha256());
        response.setNotBefore(certificate.getNotBefore());
        response.setNotAfter(certificate.getNotAfter());
        response.setStatus(certificate.getStatus() != null ? certificate.getStatus().name() : null);
        if (certificate.getGroup() != null) {
            response.setGroupId(certificate.getGroup().getId());
            response.setGroupName(certificate.getGroup().getName());
        }
        if (certificate.getCreatedBy() != null) {
            response.setCreatedBy(certificate.getCreatedBy().getId());
            response.setCreatedByName(certificate.getCreatedBy().getName());
        }
        response.setCreatedAt(certificate.getCreatedAt());
        response.setUpdatedAt(certificate.getUpdatedAt());
        if (certificate.getNotAfter() != null) {
            int days = (int) ChronoUnit.DAYS.between(Instant.now(), certificate.getNotAfter());
            response.setDaysUntilExpiry(days);
            response.setIsExpiringSoon(days <= certificate.getThresholdDays());
        }
        return response;
    }

    /**
     * Validate certificate access for a user.
     *
     * @param certId the certificate ID
     * @param userId the user ID
     * @throws AccessDeniedException if user doesn't have access
     */
    @Transactional(readOnly = true)
    public void validateCertificateAccess(Long certId, Long userId) {
        Certificate certificate = certificateRepository.findById(certId)
                .orElseThrow(() -> CertificateException.notFound(certId));

        // Admin users can access all certificates (check via role)
        // For now, check group membership

        if (certificate.getGroup() == null) {
            // No group - only creator can access
            if (certificate.getCreatedBy() == null || 
                !certificate.getCreatedBy().getId().equals(userId)) {
                throw AccessDeniedException.certificateAccessDenied(certId, userId);
            }
            return;
        }

        // Check if user is a member of the group
        if (!groupService.isMemberOfGroup(userId, certificate.getGroup().getId())) {
            throw AccessDeniedException.certificateAccessDenied(certId, userId);
        }
    }

    /**
     * Save a certificate to the database.
     */
    private Certificate saveCertificate(X509Certificate cert, String sourceUrl, Long groupId, 
                                        Integer thresholdDays, User user, String sourceType) {
        Map<String, Object> metadata = parserService.extractMetadata(cert);
        String fingerprint = (String) metadata.get("fingerprintSha256");

        // Check for duplicate
        if (certificateRepository.existsByFingerprintSha256(fingerprint)) {
            throw CertificateException.alreadyExists(fingerprint);
        }

        Certificate certificate = new Certificate();
        certificate.setCommonName((String) metadata.get("commonName"));
        certificate.setIssuer((String) metadata.get("issuer"));
        certificate.setSubject((String) metadata.get("subject"));
        certificate.setSerialNumber((String) metadata.get("serialNumber"));
        certificate.setFingerprintSha256(fingerprint);
        certificate.setNotBefore((Instant) metadata.get("notBefore"));
        certificate.setNotAfter((Instant) metadata.get("notAfter"));
        certificate.setThresholdDays(thresholdDays != null ? thresholdDays : 30);
        certificate.setSourceUrl(sourceUrl);
        certificate.setCreatedBy(user);

        // Set group if provided
        if (groupId != null) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> ResourceNotFoundException.groupNotFound(groupId));
            certificate.setGroup(group);
        }

        // Update status
        updateCertificateStatus(certificate);

        Certificate saved = certificateRepository.save(certificate);

        log.info("Saved certificate: {} with fingerprint: {}", 
                certificate.getCommonName(), fingerprint);

        auditService.logAction(
                user.getId(),
                "CREATE_CERTIFICATE",
                "CERTIFICATE",
                saved.getId(),
                "Created certificate from " + sourceType + ": " + certificate.getCommonName(),
                null
        );

        return saved;
    }

    /**
     * Update certificate status based on expiry.
     */
    private void updateCertificateStatus(Certificate certificate) {
        Instant now = Instant.now();
        if (certificate.getNotAfter().isBefore(now)) {
            certificate.setStatus(Certificate.Status.EXPIRED);
        } else {
            certificate.setStatus(Certificate.Status.ACTIVE);
        }
    }

    /**
     * Validate group access for a user.
     */
    private void validateGroupAccess(Long groupId, Long userId) {
        if (!groupService.isMemberOfGroup(userId, groupId)) {
            throw AccessDeniedException.groupAccessDenied(groupId, userId);
        }
    }
}
