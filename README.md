# AI Job Agent

An AI-powered automated job application system built with a **microservices architecture**. It scrapes job listings, analyzes them with GPT-4o, matches against your resume, tailors resumes and cover letters for top matches, generates downloadable PDFs, and sends daily email digests.

---

## Architecture

```
                         ┌────────────────────┐
                         │   React Frontend   │
                         │    (Vite + TW)     │
                         └────────┬───────────┘
                                  │ HTTP
                         ┌────────▼───────────┐
                         │   Nginx (Proxy)    │
                         │   Rate Limiting    │
                         └────────┬───────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
    ┌─────────▼────────┐  ┌──────▼──────┐  ┌────────▼─────────┐
    │   API Gateway    │  │  Scraper    │  │  (Internal only) │
    │   :8080          │  │  Service    │  │  Health endpoints│
    │  ─────────────── │  │  :8000      │  └──────────────────┘
    │  Auth + JWT      │  │  (Python)   │
    │  REST Controllers│  │  Playwright │
    │  Resume Upload   │  └──────┬──────┘
    │  Job Browsing    │         │
    │  App Tracker     │         │ Publish jobs.raw
    └────────┬─────────┘         │
             │                   ▼
    ┌────────▼──────────────────────────────────┐
    │              RabbitMQ                      │
    │  jobs.raw → jobs.analyze → jobs.match     │
    │  → jobs.tailor → notifications            │
    └────────┬──────┬───────────┬───────────────┘
             │      │           │
    ┌────────▼──┐ ┌─▼────────┐ ┌▼──────────────┐
    │   Job     │ │ Document │ │ Notification  │
    │ Processor │ │ Service  │ │ Service       │
    │  :8081    │ │  :8082   │ │  :8083        │
    │ ───────── │ │ ──────── │ │ ──────────    │
    │ Raw→DB    │ │ Tailor   │ │ Email/SMTP    │
    │ AI Analyze│ │ Cover Ltr│ │ Daily Digest  │
    │ Match     │ │ PDF Gen  │ │ Match Alerts  │
    └───────────┘ └──────────┘ └───────────────┘
             │         │              │
    ┌────────▼─────────▼──────────────▼─────────┐
    │              MySQL 8.0                     │
    │          (17 tables, shared DB)            │
    └───────────────────┬───────────────────────┘
                        │
    ┌───────────────────▼───────────────────────┐
    │              Redis 7                       │
    │          (JWT blocklist, cache)            │
    └───────────────────────────────────────────┘
```

### Microservices

| Service | Port | Role |
|---------|------|------|
| **api-gateway** | 8080 | REST API, JWT auth, controllers, Swagger |
| **job-processor** | 8081 | Consumes `jobs.raw`, `jobs.analyze`, `jobs.match` queues |
| **document-service** | 8082 | Consumes `jobs.tailor`, generates PDFs |
| **notification-service** | 8083 | Consumes `notifications`, sends emails, daily digest |
| **scraper-service** | 8000 | Python/FastAPI, scrapes Indeed/LinkedIn/Naukri |
| **frontend** | 3000 | React 18 SPA with Tailwind CSS |
| **nginx** | 80 | Reverse proxy, rate limiting, security headers |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring AMQP |
| Frontend | React 18, Vite, Tailwind CSS, React Router 6 |
| Scraper | Python 3.11, FastAPI, Playwright, httpx |
| Database | MySQL 8.0 (17 tables) |
| Cache | Redis 7 |
| Message Broker | RabbitMQ 3.12 (5 queues + DLQ) |
| AI | OpenAI GPT-4o / GPT-4o-mini |
| PDF | OpenPDF 1.3.35 |
| Resume Parsing | Apache Tika 3.2 |
| Proxy | Nginx (rate limiting, security headers) |
| Containerization | Docker, Docker Compose |

---

## Quick Start with Docker

```bash
git clone https://github.com/aryanjais1234/ai-job-agent.git
cd ai-job-agent
cp .env.example .env
# Edit .env: set JWT_SECRET, OPENAI_API_KEY, MAIL_USERNAME, MAIL_PASSWORD
docker-compose up -d
```

Access: http://localhost (Nginx) · http://localhost:8080/swagger-ui.html (API docs) · http://localhost:15672 (RabbitMQ)

---

## Local Development

```bash
# Backend (multi-module Maven — run from backend/)
cd backend && mvn clean compile && mvn test

# Run each service in a separate terminal:
cd api-gateway && mvn spring-boot:run       # :8080
cd job-processor && mvn spring-boot:run     # :8081
cd document-service && mvn spring-boot:run  # :8082
cd notification-service && mvn spring-boot:run  # :8083

# Scraper
cd scraper-service && pip install -r requirements.txt && uvicorn main:app --port 8000

# Frontend
cd frontend && npm install && npm run dev   # :5173
```

---

## Project Structure

```
ai-job-agent/
├── backend/
│   ├── pom.xml                    # Parent POM (multi-module)
│   ├── common/                    # Shared: entities, repos, DTOs, utils
│   ├── api-gateway/               # REST API, auth, controllers
│   ├── job-processor/             # Queue consumers: raw, analyze, match
│   ├── document-service/          # Tailoring, cover letter, PDF
│   └── notification-service/      # Email, daily digest, alerts
├── scraper-service/               # Python/FastAPI scraper
├── frontend/                      # React 18 + Vite + Tailwind
├── database/init.sql              # Schema (17 tables)
├── nginx/                         # Reverse proxy config
├── docker-compose.yml             # Full stack orchestration (10 containers)
├── .env.example                   # Environment template
└── docs/                          # BRD, SRS, PRD, HLD, LLD, API, DB, etc.
```

---

## License

MIT
