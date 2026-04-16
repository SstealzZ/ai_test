package com.demo.tlsalert.repository;

import com.demo.tlsalert.domain.AlertEntity;
import com.demo.tlsalert.domain.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {
    boolean existsByCertificateIdAndAlertTypeAndTriggeredAtAfter(UUID certificateId, AlertType alertType, Instant triggeredAt);

    List<AlertEntity> findByCertificateGroupNameOrderByTriggeredAtDesc(String groupName);
}
