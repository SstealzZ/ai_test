# TLS Certificate Expiration Alert Service

A Spring Boot application that monitors TLS certificates, alerts operators before expiration, and enforces group-based access control with OAuth2 / JWT.

## Features

- **OAuth2 / JWT Authentication** – Secure REST API with bearer tokens
- **Group Isolation** – Certificates are visible only within the user's group
- **Role-Based Access Control** – `CERT_ADMIN` can add certificates; `CERT_VIEWER` can list them
- **Certificate Import** – Upload `.cer`/`.crt`/`.pem` files or fetch directly from a URL
- **Expiry Tracking** – Certificates listed sorted by expiry date (soonest first)
- **Configurable Alerts** – Global threshold (default 30 days); scheduled daily check generates alert records
- **SPA Frontend** – Single-page vanilla-JS UI served from `/`

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Security 6 + OAuth2 Resource Server
- Spring Data JPA
- H2 Database (in-memory, dev mode)
- Maven
- JJWT

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+

### Run

```bash
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

Open the browser and navigate to `http://localhost:8080` to use the SPA.

### Demo Accounts

| Username | Password | Group         | Role        |
|----------|----------|---------------|-------------|
| alice    | password | Platform-Ops  | CERT_ADMIN  |
| bob      | password | Platform-Ops  | CERT_VIEWER |
| charlie  | password | Security-Team | CERT_ADMIN  |

### API Overview

All endpoints except `/api/v1/auth/login` require a valid JWT in the `Authorization: Bearer <token>` header.

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/v1/auth/login` | Public |
| POST | `/api/v1/certificates/upload` | CERT_ADMIN |
| POST | `/api/v1/certificates/from-url` | CERT_ADMIN |
| GET | `/api/v1/certificates` | Any group member |
| GET | `/api/v1/certificates/expiring?days=30` | Any group member |
| GET | `/api/v1/alerts` | Any group member |
| GET | `/api/v1/alerts/config` | Any group member |
| PUT | `/api/v1/alerts/config?thresholdDays=30` | CERT_ADMIN |

### Example: Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}'
```

### Example: Add Certificate from URL

```bash
curl -X POST "http://localhost:8080/api/v1/certificates/from-url?url=https://google.com" \
  -H "Authorization: Bearer <token>"
```

### Example: Upload Certificate File

```bash
curl -X POST http://localhost:8080/api/v1/certificates/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@example.cer"
```

## Design Notes

- **Simplified OAuth2**: The application issues its own JWTs via `/api/v1/auth/login`. In production, delegate to an external IdP (Keycloak, Auth0, etc.) and switch to a standard `JwtDecoder` configuration.
- **H2 Console**: Available at `/h2-console` (JDBC URL: `jdbc:h2:mem:certdb`).
- **Alert Scheduler**: Runs daily at 09:00 (`0 0 9 * * ?`). For demo purposes, you can also trigger alert creation by adding certificates close to expiry.
- **Group Model**: Each user belongs to exactly one group. This keeps the demonstration focused while satisfying the group-isolation requirement.

## Architecture

```
┌─────────────────┐
│   SPA Frontend  │
│  (static HTML)  │
└────────┬────────┘
         │ REST + JWT
┌────────▼────────┐
│  REST Controllers│
│  - Certificate   │
│  - Alert         │
│  - Auth          │
└────────┬────────┘
         │
┌────────▼────────┐
│    Services      │
│  - Certificate   │
│  - Alert         │
│  - Jwt           │
└────────┬────────┘
         │
┌────────▼────────┐
│   Repositories   │
│  (Spring Data)   │
└────────┬────────┘
         │
┌────────▼────────┐
│   H2 Database    │
└─────────────────┘
```

## Tests

```bash
mvn test
```

## License

MIT
