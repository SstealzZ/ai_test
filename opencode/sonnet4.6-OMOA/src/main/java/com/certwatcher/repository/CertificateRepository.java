package com.certwatcher.repository;

import com.certwatcher.domain.Certificate;
import com.certwatcher.domain.CertGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByGroupOrderByNotAfterDesc(CertGroup group);

    @Query("""
            SELECT c FROM Certificate c
            WHERE c.notAfter BETWEEN :now AND :deadline
            """)
    List<Certificate> findExpiringBefore(@Param("now") Instant now,
                                         @Param("deadline") Instant deadline);
}
