-- V2__test_data.sql
-- Sample test data for development and testing
-- H2 compatible syntax

-- Insert sample users (3 users)
INSERT INTO users (email, password, name, given_name, family_name, picture, created_at, updated_at, last_login_at) VALUES
('alice@example.com', '$2a$10$9kP4YmbA0JNTmS3hcRt8/uy.vwignbQ5nfeS89IovWK1O9YRD3Zhe', 'Alice Johnson', 'Alice', 'Johnson', 'https://example.com/avatars/alice.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bob@example.com', '$2a$10$9kP4YmbA0JNTmS3hcRt8/uy.vwignbQ5nfeS89IovWK1O9YRD3Zhe', 'Bob Smith', 'Bob', 'Smith', 'https://example.com/avatars/bob.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('charlie@example.com', '$2a$10$WVLyNEAqj0aeA3HNAceDo.HjuTFDGmM/lVOA1S835EVB1OitD.mYa', 'Charlie Brown', 'Charlie', 'Brown', 'https://example.com/avatars/charlie.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample groups (2 groups)
INSERT INTO groups (name, description, created_by, created_at, updated_at) VALUES
('DevOps Team', 'Development and Operations team members', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Security Team', 'Security and compliance team members', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert group memberships with different roles
INSERT INTO group_members (group_id, user_id, role, created_at, updated_at) VALUES
(1, 1, 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 2, 'MEMBER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, 'OWNER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'MEMBER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample certificates with various expiration states
-- Certificate 1: Valid certificate (expires in 90 days)
INSERT INTO certificates (common_name, issuer, subject, serial_number, fingerprint_sha256, not_before, not_after, pem_certificate, status, group_id, created_by, threshold_days, created_at, updated_at) VALUES
('api.example.com', 'CN=Example CA, O=Example Inc, C=US', 'CN=api.example.com, O=Example Inc, C=US',
 'CERT-001-2026',
 'a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456',
 CURRENT_TIMESTAMP - INTERVAL '365' DAY,
 CURRENT_TIMESTAMP + INTERVAL '90' DAY,
 '-----BEGIN CERTIFICATE-----
MIIDXTCCAkWgAwIBAgIJAJC1HiIAZAiUMA0GCSqGSIb3Qw0teleEBBQUAMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwHhcNMTYwODI0MTY0NjA3WhcNMjYwODIyMTY0NjA3WjBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwU7OTOlkHDE8+0K5
-----END CERTIFICATE-----',
 'ACTIVE', 1, 1, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Certificate 2: Expiring soon (expires in 7 days)
INSERT INTO certificates (common_name, issuer, subject, serial_number, fingerprint_sha256, not_before, not_after, pem_certificate, status, group_id, created_by, threshold_days, created_at, updated_at) VALUES
('web.example.com', 'CN=Example CA, O=Example Inc, C=US', 'CN=web.example.com, O=Example Inc, C=US',
 'CERT-002-2026',
 'b2c3d4e5f67890123456789012345678901abcdef2345678901abcdef1234567',
 CURRENT_TIMESTAMP - INTERVAL '358' DAY,
 CURRENT_TIMESTAMP + INTERVAL '7' DAY,
 '-----BEGIN CERTIFICATE-----
MIIDYTCCAkmgAwIBAgIJAJC1HiIAZAiVMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwHhcNMTYwODI0MTY0NjA3WhcNMjYwODIyMTY0NjA3WjBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwU7OTOlkHDE8+0K6
-----END CERTIFICATE-----',
 'ACTIVE', 1, 2, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Certificate 3: Expired certificate
INSERT INTO certificates (common_name, issuer, subject, serial_number, fingerprint_sha256, not_before, not_after, pem_certificate, status, group_id, created_by, threshold_days, created_at, updated_at) VALUES
('old.example.com', 'CN=Example CA, O=Example Inc, C=US', 'CN=old.example.com, O=Example Inc, C=US',
 'CERT-003-2025',
 'c3d4e5f678901234567890123456789012abcdef3456789012abcdef12345678',
 CURRENT_TIMESTAMP - INTERVAL '400' DAY,
 CURRENT_TIMESTAMP - INTERVAL '35' DAY,
 '-----BEGIN CERTIFICATE-----
MIIDZTCCAk2gAwIBAgIJAJC1HiIAZAiWMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwHhcNMTYwODI0MTY0NjA3WhcNMjYwODIyMTY0NjA3WjBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwU7OTOlkHDE8+0K7
-----END CERTIFICATE-----',
 'EXPIRED', 1, 1, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Certificate 4: Valid certificate in Security Team (expires in 180 days)
INSERT INTO certificates (common_name, issuer, subject, serial_number, fingerprint_sha256, not_before, not_after, pem_certificate, status, group_id, created_by, threshold_days, created_at, updated_at) VALUES
('secure.example.com', 'CN=Example CA, O=Example Inc, C=US', 'CN=secure.example.com, O=Example Inc, C=US',
 'CERT-004-2026',
 'd4e5f6789012345678901234567890123abcdef4567890123abcdef123456789',
 CURRENT_TIMESTAMP - INTERVAL '185' DAY,
 CURRENT_TIMESTAMP + INTERVAL '180' DAY,
 '-----BEGIN CERTIFICATE-----
MIIDZjCCAk6gAwIBAgIJAJC1HiIAZAiXMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwHhcNMTYwODI0MTY0NjA3WhcNMjYwODIyMTY0NjA3WjBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwU7OTOlkHDE8+0K8
-----END CERTIFICATE-----',
 'ACTIVE', 2, 3, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Certificate 5: Expiring very soon (expires in 3 days)
INSERT INTO certificates (common_name, issuer, subject, serial_number, fingerprint_sha256, not_before, not_after, pem_certificate, status, group_id, created_by, threshold_days, created_at, updated_at) VALUES
('critical.example.com', 'CN=Example CA, O=Example Inc, C=US', 'CN=critical.example.com, O=Example Inc, C=US',
 'CERT-005-2026',
 'e5f67890123456789012345678901234abcdef5678901234abcdef1234567890',
 CURRENT_TIMESTAMP - INTERVAL '362' DAY,
 CURRENT_TIMESTAMP + INTERVAL '3' DAY,
 '-----BEGIN CERTIFICATE-----
MIIDZzCCAlCgAwIBAgIJAJC1HiIAZAiYMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwHhcNMTYwODI0MTY0NjA3WhcNMjYwODIyMTY0NjA3WjBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwU7OTOlkHDE8+0K9
-----END CERTIFICATE-----',
 'ACTIVE', 2, 3, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert certificate alerts
INSERT INTO certificate_alerts (certificate_id, alert_type, message, days_remaining, recipient_email, sent_at, is_read, user_id, created_at) VALUES
(2, 'EXPIRY_WARNING', 'Certificate web.example.com will expire in 7 days', 7, 'alice@example.com', CURRENT_TIMESTAMP, false, 1, CURRENT_TIMESTAMP),
(5, 'EXPIRY_WARNING', 'Certificate critical.example.com will expire in 3 days', 3, 'charlie@example.com', CURRENT_TIMESTAMP, false, 3, CURRENT_TIMESTAMP),
(3, 'EXPIRED', 'Certificate old.example.com has expired', 0, 'alice@example.com', CURRENT_TIMESTAMP, true, 1, CURRENT_TIMESTAMP);

-- Insert audit log entries
INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_value, new_value, ip_address, user_agent, created_at) VALUES
(1, 'CREATE', 'CERTIFICATE', 1, NULL, '{"commonName": "api.example.com"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', CURRENT_TIMESTAMP - INTERVAL '365' DAY),
(2, 'CREATE', 'CERTIFICATE', 2, NULL, '{"commonName": "web.example.com"}', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', CURRENT_TIMESTAMP - INTERVAL '358' DAY),
(1, 'UPDATE', 'CERTIFICATE', 3, '{"status": "ACTIVE"}', '{"status": "EXPIRED"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', CURRENT_TIMESTAMP - INTERVAL '35' DAY),
(3, 'CREATE', 'CERTIFICATE', 4, NULL, '{"commonName": "secure.example.com"}', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64)', CURRENT_TIMESTAMP - INTERVAL '185' DAY),
(3, 'CREATE', 'CERTIFICATE', 5, NULL, '{"commonName": "critical.example.com"}', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64)', CURRENT_TIMESTAMP - INTERVAL '362' DAY),
(1, 'LOGIN', 'USER', 1, NULL, NULL, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', CURRENT_TIMESTAMP),
(2, 'LOGIN', 'USER', 2, NULL, NULL, '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', CURRENT_TIMESTAMP);
