# Configuration Reference
## AI Job Agent — Automated Job Application System
**Version:** 1.0

---

## 1. Spring Boot — application.yml

```yaml
spring:
  application:
    name: ai-job-agent

  # ── Database ────────────────────────────────────────────────────
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:aijobagent}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8mb4
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:changeme}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      connection-timeout: 20000
      pool-name: AiJobAgentHikari

  # ── JPA ─────────────────────────────────────────────────────────
  jpa:
    hibernate:
      ddl-auto: validate         # Never use create/create-drop in production
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        default_batch_fetch_size: 20
        jdbc:
          batch_size: 25

  # ── Mail ─────────────────────────────────────────────────────────
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

  # ── RabbitMQ ─────────────────────────────────────────────────────
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: ${RABBITMQ_VHOST:/}
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 1
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3
          multiplier: 2.0

  # ── Redis ────────────────────────────────────────────────────────
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 10
          max-idle: 5
          min-idle: 1

  # ── File Upload ──────────────────────────────────────────────────
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 12MB

# ── Server ──────────────────────────────────────────────────────────
server:
  port: 8080
  error:
    include-message: always
    include-binding-errors: always
  compression:
    enabled: true
    mime-types: application/json,text/plain

# ── Application Custom Config ────────────────────────────────────────
app:
  # JWT
  jwt:
    secret: ${JWT_SECRET}              # min 256-bit base64-encoded key
    access-token-expiry-ms: 86400000   # 24 hours
    refresh-token-expiry-ms: 604800000 # 7 days

  # AI (OpenAI-compatible)
  ai:
    api-key: ${OPENAI_API_KEY}
    base-url: ${AI_BASE_URL:https://api.openai.com/v1}
    analysis-model: ${AI_ANALYSIS_MODEL:gpt-4o-mini}
    tailor-model: ${AI_TAILOR_MODEL:gpt-4o}
    max-tokens: 4096
    timeout-seconds: 60
    retry-max-attempts: 3

  # Scraper Service
  scraper:
    base-url: ${SCRAPER_SERVICE_URL:http://scraper-service:8000}
    timeout-seconds: 300

  # Storage
  storage:
    resume-upload-dir: ${RESUME_UPLOAD_DIR:/app/storage/resumes}
    pdf-output-dir: ${PDF_OUTPUT_DIR:/app/storage/pdfs}

  # Match Score Thresholds
  matching:
    minimum-score-threshold: 70.0
    auto-tailor-threshold: 75.0

  # Notifications
  notification:
    from-email: ${NOTIFICATION_FROM_EMAIL:noreply@aijobagent.dev}
    from-name: "AI Job Agent"
    daily-digest-cron: ${DAILY_DIGEST_CRON:0 0 7 * * *}    # 7 AM daily
    scrape-cron: ${SCRAPE_CRON:0 0 6 * * *}                # 6 AM daily

# ── Logging ──────────────────────────────────────────────────────────
logging:
  level:
    com.aryanjais.aijobagent: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# ── Springdoc OpenAPI ────────────────────────────────────────────────
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: ${SWAGGER_ENABLED:false}   # Disable in production
```

---

## 2. docker-compose.yml

```yaml
version: '3.9'

networks:
  aijobagent-net:
    driver: bridge

volumes:
  mysql_data:
  redis_data:
  rabbitmq_data:
  resume_storage:
  pdf_storage:

services:

  # ── MySQL ──────────────────────────────────────────────────────────
  mysql:
    image: mysql:8.0
    container_name: aijobagent-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD:-changeme}
      MYSQL_DATABASE: ${DB_NAME:-aijobagent}
      MYSQL_USER: ${DB_USERNAME:-aijobagent}
      MYSQL_PASSWORD: ${DB_PASSWORD:-changeme}
    ports:
      - "${DB_PORT:-3306}:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    networks:
      - aijobagent-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${DB_ROOT_PASSWORD:-changeme}"]
      interval: 10s
      timeout: 5s
      retries: 10

  # ── Redis ──────────────────────────────────────────────────────────
  redis:
    image: redis:7-alpine
    container_name: aijobagent-redis
    restart: unless-stopped
    command: redis-server --requirepass ${REDIS_PASSWORD:-}
    ports:
      - "${REDIS_PORT:-6379}:6379"
    volumes:
      - redis_data:/data
    networks:
      - aijobagent-net
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  # ── RabbitMQ ───────────────────────────────────────────────────────
  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    container_name: aijobagent-rabbitmq
    restart: unless-stopped
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USERNAME:-guest}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD:-guest}
    ports:
      - "${RABBITMQ_PORT:-5672}:5672"
      - "15672:15672"    # Management UI
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - aijobagent-net
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 15s
      timeout: 10s
      retries: 10

  # ── Spring Boot Backend ────────────────────────────────────────────
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: aijobagent-backend
    restart: unless-stopped
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: ${DB_NAME:-aijobagent}
      DB_USERNAME: ${DB_USERNAME:-aijobagent}
      DB_PASSWORD: ${DB_PASSWORD:-changeme}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD:-}
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: ${RABBITMQ_USERNAME:-guest}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD:-guest}
      JWT_SECRET: ${JWT_SECRET}
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
      SCRAPER_SERVICE_URL: http://scraper-service:8000
    ports:
      - "8080:8080"
    volumes:
      - resume_storage:/app/storage/resumes
      - pdf_storage:/app/storage/pdfs
    networks:
      - aijobagent-net

  # ── FastAPI Scraper ────────────────────────────────────────────────
  scraper-service:
    build:
      context: ./scraper-service
      dockerfile: Dockerfile
    container_name: aijobagent-scraper
    restart: unless-stopped
    depends_on:
      rabbitmq:
        condition: service_healthy
    environment:
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: ${RABBITMQ_USERNAME:-guest}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD:-guest}
    ports:
      - "8000:8000"
    networks:
      - aijobagent-net

  # ── React Frontend ─────────────────────────────────────────────────
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
      args:
        VITE_API_BASE_URL: ${VITE_API_BASE_URL:-http://localhost:8080}
    container_name: aijobagent-frontend
    restart: unless-stopped
    ports:
      - "3000:80"
    networks:
      - aijobagent-net
```

---

## 3. Environment Variables Reference

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_HOST` | No | `localhost` | MySQL hostname |
| `DB_PORT` | No | `3306` | MySQL port |
| `DB_NAME` | No | `aijobagent` | MySQL database name |
| `DB_USERNAME` | **Yes** | — | MySQL application user |
| `DB_PASSWORD` | **Yes** | — | MySQL application password |
| `DB_ROOT_PASSWORD` | **Yes** | — | MySQL root password (Docker only) |
| `REDIS_HOST` | No | `localhost` | Redis hostname |
| `REDIS_PORT` | No | `6379` | Redis port |
| `REDIS_PASSWORD` | No | ` ` | Redis password (empty = no auth) |
| `RABBITMQ_HOST` | No | `localhost` | RabbitMQ hostname |
| `RABBITMQ_PORT` | No | `5672` | RabbitMQ AMQP port |
| `RABBITMQ_USERNAME` | No | `guest` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | No | `guest` | RabbitMQ password |
| `JWT_SECRET` | **Yes** | — | HS256 signing key (min 256-bit base64) |
| `OPENAI_API_KEY` | **Yes** | — | OpenAI API key (`sk-...`) |
| `AI_BASE_URL` | No | OpenAI | Override for self-hosted models |
| `AI_ANALYSIS_MODEL` | No | `gpt-4o-mini` | Model for JD analysis |
| `AI_TAILOR_MODEL` | No | `gpt-4o` | Model for resume tailoring |
| `MAIL_HOST` | No | `smtp.gmail.com` | SMTP server hostname |
| `MAIL_PORT` | No | `587` | SMTP server port |
| `MAIL_USERNAME` | **Yes** | — | SMTP authentication username |
| `MAIL_PASSWORD` | **Yes** | — | SMTP authentication password (App Password) |
| `NOTIFICATION_FROM_EMAIL` | No | `noreply@aijobagent.dev` | Sender email address |
| `DAILY_DIGEST_CRON` | No | `0 0 7 * * *` | Cron for daily digest (7 AM) |
| `SCRAPE_CRON` | No | `0 0 6 * * *` | Cron for daily scrape (6 AM) |
| `SWAGGER_ENABLED` | No | `false` | Enable Swagger UI (dev only) |
| `VITE_API_BASE_URL` | No | `http://localhost:8080` | Frontend API base URL |

---

*Document End — Configuration v1.0*
