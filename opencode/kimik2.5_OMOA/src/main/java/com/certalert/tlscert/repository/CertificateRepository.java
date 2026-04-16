package com.certalert.tlscert.repository;

import com.certalert.tlscert.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    @Query("SELECT c FROM Certificate c WHERE c.group.id = :groupId ORDER BY c.validUntil ASC")
    List<Certificate> findByGroupIdOrderByValidUntilAsc(@Param("groupId") UUID groupId);

    @Query("SELECT c FROM Certificate c WHERE c.group.id = :groupId AND c.validUntil <= :threshold ORDER BY c.validUntil ASC")
    List<Certificate> findByGroupIdAndValidUntilBeforeOrderByValidUntilAsc(
            @Param("groupId") UUID groupId,
            @Param("threshold") Instant threshold);

    @Query("SELECT c FROM Certificate c WHERE c.validUntil <= :threshold ORDER BY c.validUntil ASC")
    List<Certificate> findByValidUntilBeforeOrderByValidUntilAsc(@Param("threshold") Instant threshold);
}
