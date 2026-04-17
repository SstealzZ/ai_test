package com.example.certmanager.service;

import com.example.certmanager.entity.AuditLog;
import com.example.certmanager.entity.User;
import com.example.certmanager.repository.AuditLogRepository;
import com.example.certmanager.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for audit logging.
 */
@Service
@Transactional
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository,
                        UserRepository userRepository,
                        ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Log an action.
     *
     * @param userId the user ID performing the action
     * @param action the action performed
     * @param entityType the type of entity affected
     * @param entityId the ID of the entity affected
     * @param details additional details about the action
     * @param ipAddress the IP address of the user
     */
    public void logAction(Long userId, String action, String entityType, Long entityId,
                          String details, String ipAddress) {
        log.debug("Audit log: userId={}, action={}, entityType={}, entityId={}",
                userId, action, entityType, entityId);

        AuditLog auditLog = new AuditLog();

        if (userId != null) {
            userRepository.findById(userId).ifPresent(auditLog::setUser);
        }

        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setIpAddress(ipAddress);

        auditLogRepository.save(auditLog);
    }

    /**
     * Log an action with old and new values.
     *
     * @param userId the user ID performing the action
     * @param action the action performed
     * @param entityType the type of entity affected
     * @param entityId the ID of the entity affected
     * @param oldValue the old value (will be serialized to JSON)
     * @param newValue the new value (will be serialized to JSON)
     * @param ipAddress the IP address of the user
     */
    public void logActionWithValues(Long userId, String action, String entityType, Long entityId,
                                     Object oldValue, Object newValue, String ipAddress) {
        AuditLog auditLog = new AuditLog();

        if (userId != null) {
            userRepository.findById(userId).ifPresent(auditLog::setUser);
        }

        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setIpAddress(ipAddress);

        try {
            if (oldValue != null) {
                auditLog.setOldValue(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                auditLog.setNewValue(objectMapper.writeValueAsString(newValue));
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit log values", e);
        }

        auditLogRepository.save(auditLog);
    }

    /**
     * Get audit log for an entity.
     *
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param pageable pagination info
     * @return page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLog(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntity(entityType, entityId, pageable);
    }

    /**
     * Get audit log for a user.
     *
     * @param userId the user ID
     * @param pageable pagination info
     * @return page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getUserAuditLog(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * Get audit log count for an entity.
     *
     * @param entityType the entity type
     * @param entityId the entity ID
     * @return count of audit logs
     */
    @Transactional(readOnly = true)
    public long getAuditLogCount(String entityType, Long entityId) {
        return auditLogRepository.countByEntity(entityType, entityId);
    }
}
