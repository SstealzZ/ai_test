package com.example.certmanager.repository;

import com.example.certmanager.entity.Certificate;
import com.example.certmanager.entity.CertificateAlert;
import com.example.certmanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CertificateAlertRepository extends JpaRepository<CertificateAlert, Long> {

    Page<CertificateAlert> findByUser(User user, Pageable pageable);

    Page<CertificateAlert> findByUserAndIsRead(User user, Boolean isRead, Pageable pageable);

    List<CertificateAlert> findByUserOrderByCreatedAtDesc(User user);

    List<CertificateAlert> findByCertificate(Certificate certificate);

    @Query("SELECT COUNT(ca) FROM CertificateAlert ca WHERE ca.user = :user AND ca.isRead = false")
    long countUnreadByUser(@Param("user") User user);

    @Query("SELECT COUNT(ca) FROM CertificateAlert ca WHERE ca.user = :user")
    long countByUser(@Param("user") User user);

    @Query("SELECT ca FROM CertificateAlert ca WHERE ca.certificate = :cert AND ca.user = :user ORDER BY ca.createdAt DESC")
    List<CertificateAlert> findByCertificateAndUser(@Param("cert") Certificate cert, @Param("user") User user);

    void deleteByCertificate(Certificate certificate);

    List<CertificateAlert> findByCertificateIdAndRecipientEmailAndSentAtAfter(Long certificateId, String recipientEmail, Instant sentAtAfter);

    @Query("SELECT ca FROM CertificateAlert ca WHERE ca.certificate.id = :certificateId AND ca.alertType = :alertType AND ca.recipientEmail = :recipientEmail ORDER BY ca.sentAt DESC")
    List<CertificateAlert> findLatestAlertsByCertificateAndTypeAndEmail(@Param("certificateId") Long certificateId, @Param("alertType") CertificateAlert.AlertType alertType, @Param("recipientEmail") String recipientEmail);
}
