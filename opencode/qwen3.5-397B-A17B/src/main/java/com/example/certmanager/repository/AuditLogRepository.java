package com.example.certmanager.repository;

import com.example.certmanager.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.entityId = :entityId ORDER BY al.createdAt DESC")
    Page<AuditLog> findByEntity(@Param("entityType") String entityType, 
                                 @Param("entityId") Long entityId, 
                                 Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.user.id = :userId ORDER BY al.createdAt DESC")
    Page<AuditLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :start AND :end ORDER BY al.createdAt DESC")
    List<AuditLog> findByTimeRange(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.entityType = :entityType AND al.entityId = :entityId")
    long countByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);
}
