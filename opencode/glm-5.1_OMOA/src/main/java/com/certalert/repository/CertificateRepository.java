package com.certalert.repository;

import com.certalert.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByGroupIdOrderByNotAfterDesc(Long groupId);
    List<Certificate> findByNotAfterBefore(Instant date);
    List<Certificate> findByNotAfterBetween(Instant start, Instant end);
    boolean existsBySerialNumber(String serialNumber);
}