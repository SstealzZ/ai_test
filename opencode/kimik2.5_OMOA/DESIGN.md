# TLS Certificate Expiration Alert Service
## Functional Specification & Architecture Design

### 1. Overview
A Spring Boot-based service that monitors TLS certificates, alerts operators before expiration, and enforces group-based access control.

### 2. Core Features

#### 2.1 Authentication & Authorization
- **OAuth2 / JWT-based authentication** using Spring Security Resource Server
- **Group-based isolation**: certificates are owned by a group; only group members can see them
- **Role-based permissions**:
  - `CERT_ADMIN`: can add/upload certificates to their group
  - `CERT_VIEWER`: can list certificates in their group
- A user may belong to exactly one group (simplified model) or multiple groups (flexible model)

#### 2.2 Certificate Management
- **Add certificate** via:
  - File upload (`*.cer`, `*.crt`, `*.pem`)
  - Fetch from HTTPS URL (extracts server certificate chain)
- **Parse certificate** to extract:
  - Subject / Issuer
  - Serial number
  - Valid-from / Valid-to dates
  - SHA-256 fingerprint
- **List certificates** for the user's group, sorted by expiry date descending (soonest first)

#### 2.3 Alerting
- Configurable global threshold (default: 30 days)
- Scheduled background job runs daily to detect certificates expiring within threshold
- Alert records are created; in a real system these would trigger email/webhook notifications

### 3. Data Model

```
UserAccount
  - id (UUID)
  - username (unique)
  - email
  - group_id (FK)
  - role (CERT_ADMIN | CERT_VIEWER)

Group
  - id (UUID)
  - name (unique)

Certificate
  - id (UUID)
  - group_id (FK)
  - uploaded_by_user_id (FK)
  - subject_dn
  - issuer_dn
  - serial_number
  - sha256_fingerprint
  - valid_from (Instant)
  - valid_until (Instant)
  - source_type (UPLOAD | URL)
  - source_url (optional)
  - raw_pem (TEXT)
  - created_at

AlertConfig
  - id
  - default_threshold_days (int)

CertificateAlert
  - id (UUID)
  - certificate_id (FK)
  - alert_sent_at
  - days_until_expiry (int)
  - acknowledged (boolean)
```

### 4. API Design

| Method | Path | Description | RBAC |
|--------|------|-------------|------|
| POST | /api/v1/certificates/upload | Upload certificate file | CERT_ADMIN |
| POST | /api/v1/certificates/from-url | Fetch certificate from URL | CERT_ADMIN |
| GET | /api/v1/certificates | List group certificates (sorted by expiry) | Any group member |
| GET | /api/v1/certificates/expiring | List certificates expiring within threshold | Any group member |
| GET | /api/v1/alerts/config | Get alert configuration | Any group member |
| PUT | /api/v1/alerts/config | Update alert threshold | CERT_ADMIN |

### 5. Security Design
- JWT access tokens contain `sub`, `group_id`, `role`
- A custom `GroupAuthorizationManager` ensures users can only access certificates where `certificate.group_id == user.group_id`
- Method-level security (`@PreAuthorize`) for admin actions

### 6. Technology Stack
- Java 17
- Spring Boot 3.2+
- Spring Security 6 + OAuth2 Resource Server
- Spring Data JPA
- H2 Database (dev/test)
- Maven
- Scheduled tasks with `@Scheduled`
- Frontend: Vanilla JS SPA (keeps focus on backend architecture)

### 7. Assumptions & Design Decisions
1. **Simplified OAuth2**: For this exercise, the app acts as its own authorization server with an in-memory issuer and a `/login` endpoint that returns a JWT. In production, this would delegate to an external IdP (Keycloak, Okta, etc.).
2. **Single group per user**: Keeps RBAC simple while still demonstrating group isolation.
3. **H2 in-memory DB**: Zero-config for evaluation; schema is JPA-generated.
4. **Alerting as persistent records**: Rather than integrating with SMTP, we persist alert events that an operator can query.
5. **Certificate from URL**: Uses JSSE to open an SSL socket to the target host and extract the peer certificate chain.
