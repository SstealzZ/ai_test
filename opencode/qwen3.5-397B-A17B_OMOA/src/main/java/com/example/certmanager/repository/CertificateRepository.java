package com.example.certmanager.repository;

import com.example.certmanager.entity.Certificate;
import com.example.certmanager.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByFingerprintSha256(String fingerprintSha256);

    boolean existsByFingerprintSha256(String fingerprintSha256);

    Page<Certificate> findByGroup(Group group, Pageable pageable);

    Page<Certificate> findByGroupAndStatus(Group group, Certificate.Status status, Pageable pageable);

    List<Certificate> findByGroupOrderByNotAfterDesc(Group group);

    @Query("SELECT c FROM Certificate c WHERE c.group = :group ORDER BY c.notAfter DESC")
    Page<Certificate> findByGroupSortedByExpiry(@Param("group") Group group, Pageable pageable);

    @Query("SELECT c FROM Certificate c WHERE c.group = :group AND c.status = :status ORDER BY c.notAfter DESC")
    Page<Certificate> findByGroupAndStatusSortedByExpiry(@Param("group") Group group, 
                                                          @Param("status") Certificate.Status status, 
                                                          Pageable pageable);

    @Query("SELECT c FROM Certificate c WHERE c.notAfter <= :threshold AND c.status = 'ACTIVE'")
    List<Certificate> findExpiringCertificates(@Param("threshold") Instant threshold);

    @Query("SELECT c FROM Certificate c WHERE c.notAfter < CURRENT_TIMESTAMP AND c.status = 'ACTIVE'")
    List<Certificate> findExpiredCertificates();

    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.group = :group")
    long countByGroup(@Param("group") Group group);

    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.group = :group AND c.status = :status")
    long countByGroupAndStatus(@Param("group") Group group, @Param("status") Certificate.Status status);

    @Query("SELECT c FROM Certificate c WHERE c.status = 'ACTIVE'")
    List<Certificate> findAllActive();
}
