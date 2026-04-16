package com.certalert.tlscert.repository;

import com.certalert.tlscert.entity.CertificateAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateAlertRepository extends JpaRepository<CertificateAlert, UUID> {
    List<CertificateAlert> findByCertificateGroupIdOrderByAlertSentAtDesc(UUID groupId);
    Optional<CertificateAlert> findByCertificateIdAndAcknowledgedFalse(UUID certificateId);
    Optional<CertificateAlert> findTopByCertificateIdOrderByAlertSentAtDesc(UUID certificateId);
}
