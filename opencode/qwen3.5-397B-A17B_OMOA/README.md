# Certificate Manager

Certificate Management System with Flyway and OAuth2 Security.

## Docker Development Setup

### Prerequisites

- Docker and Docker Compose installed
- Java 17+ (for local development)
- Maven 3.8+

### Quick Start with Docker

**Start all services:**
```bash
docker-compose up -d
```

This starts:
- **PostgreSQL** on port `5432`
- **MailHog** SMTP on port `1025`, UI on port `8025`
- **Application** on port `8080`

**View logs:**
```bash
docker-compose logs -f app
```

**Stop all services:**
```bash
docker-compose down
```

**Stop and remove volumes (clean slate):**
```bash
docker-compose down -v
```

### Accessing Services

| Service    | URL/Port              | Credentials                    |
|------------|-----------------------|--------------------------------|
| App        | http://localhost:8080 | OAuth2 (configured provider)   |
| PostgreSQL | localhost:5432        | certmanager / certmanager      |
| MailHog UI | http://localhost:8025 | No auth required               |

### Building the Docker Image

**Build from scratch:**
```bash
docker-compose build --no-cache
```

**Build only the app:**
```bash
docker-compose build app
```

### Local Development (Without Docker)

**Run with PostgreSQL profile:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Run tests:**
```bash
./mvnw clean test
```

### Environment Variables

The Docker setup uses these environment variables:

| Variable                      | Value                              |
|-------------------------------|------------------------------------|
| SPRING_PROFILES_ACTIVE        | docker                             |
| SPRING_DATASOURCE_URL         | jdbc:postgresql://postgres:5432/certmanager |
| SPRING_DATASOURCE_USERNAME    | certmanager                        |
| SPRING_DATASOURCE_PASSWORD    | certmanager                        |
| SPRING_MAIL_HOST              | mailhog                            |
| SPRING_MAIL_PORT              | 1025                               |

### Troubleshooting

**Database connection issues:**
```bash
# Check PostgreSQL health
docker-compose ps postgres

# View PostgreSQL logs
docker-compose logs postgres
```

**Reset database:**
```bash
docker-compose down -v
docker-compose up -d postgres
# Wait for DB to be ready, then start app
docker-compose up -d app
```
