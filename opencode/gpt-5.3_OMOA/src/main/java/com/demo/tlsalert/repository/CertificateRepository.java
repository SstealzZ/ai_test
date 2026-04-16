package com.demo.tlsalert.repository;

import com.demo.tlsalert.domain.CertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<CertificateEntity, UUID> {
    List<CertificateEntity> findByGroupNameOrderByNotAfterDesc(String groupName);

    boolean existsByGroupNameAndFingerprintSha256(String groupName, String fingerprintSha256);
}
