# CertWatcher

TLS certificate expiry alerting service built with Spring Boot 3 / Java 21.

## Quick Start

```bash
mvn spring-boot:run
```

Open http://localhost:8080/ui/certs and log in with one of the demo accounts below.

## Demo Accounts

| Username | Password | Group | Role |
|----------|----------|-------|------|
| alice    | password | ops   | CERT_ADMIN |
| bob      | password | ops   | CERT_VIEWER |
| carol    | password | dev   | CERT_ADMIN |
| dave     | password | dev   | CERT_VIEWER |

## Access Control

- **CERT_ADMIN** — can add certificates (file upload or URL fetch) and update group settings
- **CERT_VIEWER** — can only list certificates
- **Group isolation** — each user only sees certificates belonging to their own group, enforced at the JPA query level

## Features

| Feature | Endpoint |
|---------|----------|
| List certificates (desc by expiry) | `GET /api/v1/certificates` |
| Upload `.cer` / `.pem` file | `POST /api/v1/certificates/upload` |
| Fetch from hostname | `POST /api/v1/certificates/from-url` |
| Update alert threshold + webhook | `PUT /api/v1/certificates/settings` |
| Get current group settings | `GET /api/v1/certificates/settings` |

### REST API Usage

All API calls require a Bearer JWT. Obtain one from the Authorization Server:

```bash
# 1. Get authorization code via browser:
#    http://localhost:8080/oauth2/authorize?response_type=code&client_id=cert-watcher-ui&redirect_uri=http://localhost:8080/login/oauth2/code/cert-watcher-ui&scope=openid%20certs

# 2. Exchange code for token:
curl -X POST http://localhost:8080/oauth2/token \
  -u cert-watcher-ui:secret \
  -d grant_type=authorization_code \
  -d code=<CODE> \
  -d redirect_uri=http://localhost:8080/login/oauth2/code/cert-watcher-ui

# 3. Use the access_token:
curl http://localhost:8080/api/v1/certificates \
  -H "Authorization: Bearer <access_token>"
```

### Add a certificate from a URL

```bash
curl -X POST http://localhost:8080/api/v1/certificates/from-url \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"alias":"google","hostname":"www.google.com","port":443}'
```

### Configure alert threshold and webhook

```bash
curl -X PUT http://localhost:8080/api/v1/certificates/settings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"alertThresholdDays":30,"webhookUrl":"https://hooks.example.com/cert-alerts"}'
```

## Alert Mechanism

A `@Scheduled` job runs daily at 08:00 (configurable via `certwatcher.scheduler.cron`).  
For each group with a configured `webhookUrl`, it POSTs a JSON payload:

```json
{
  "group": "ops",
  "alertThresholdDays": 30,
  "expiringCertificates": [
    {
      "alias": "my-cert",
      "subjectDn": "CN=example.com",
      "notAfter": "2026-05-01T00:00:00Z",
      "daysUntilExpiry": 14,
      "source": "URL:example.com:443"
    }
  ]
}
```

## H2 Console

Available at http://localhost:8080/h2-console  
JDBC URL: `jdbc:h2:mem:certwatcher`

## Architecture

```
com.certwatcher
├── config/
│   ├── AuthorizationServerConfig   Spring Authorization Server + JWT customizer
│   ├── SecurityConfig              3 filter chains: Auth Server / API / UI
│   ├── UserDetailsServiceConfig    Bridges AppUser entity to Spring Security
│   └── DataBootstrapper            Seeds demo groups + users on startup
├── domain/
│   ├── Certificate                 JPA entity — TLS cert metadata
│   ├── CertGroup                   JPA entity — group with threshold + webhook
│   ├── AppUser                     JPA entity — user with role + group FK
│   └── UserRole                    Enum: CERT_ADMIN | CERT_VIEWER
├── repository/                     Spring Data JPA repositories
├── service/
│   └── CertificateService          Business logic, group-scoped queries
├── scheduler/
│   └── ExpiryAlertScheduler        Daily cron job, webhook delivery
├── controller/
│   ├── CertificateApiController    REST API (/api/v1/certificates/**)
│   ├── UiController                Thymeleaf MVC (/ui/**)
│   └── LoginController             Form login page
├── dto/                            Records for API I/O
└── security/
    └── CurrentUserResolver         Resolves authenticated user from SecurityContext
```

## Design Decisions

**Why group isolation at the query level?** Enforcing `WHERE cert.group = :userGroup` in the repository means even a bug in the service layer cannot expose cross-group data. Application-level filtering would be a weaker guarantee.

**Why Spring Authorization Server over Keycloak?** Zero external process required — the IdP runs in the same JVM. Ideal for a demo; production would typically extract it.

**Why webhook over email?** More composable — the webhook receiver can route to email, Slack, PagerDuty, etc. without coupling the service to a specific notification channel.

**Why in-memory H2?** Instant startup, no infrastructure, perfect for demo and testing. Switching to PostgreSQL requires only a dependency swap and a `spring.datasource.url` change — JPA/Flyway abstracts the rest.
