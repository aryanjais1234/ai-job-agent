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
| Frontend | React 18, Vite, Tailwind CSS, React Router 6, Lucide Icons |
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

The fastest way to get the entire stack running:

```bash
# 1. Clone the repository
git clone https://github.com/aryanjais1234/ai-job-agent.git
cd ai-job-agent

# 2. Create environment file
cp .env.example .env

# 3. Edit .env with your credentials (see Environment Variables below)
#    At minimum, set these required variables:
#    - DB_USERNAME / DB_PASSWORD
#    - JWT_SECRET (min 32 characters, base64-encoded)
#    - OPENAI_API_KEY (from https://platform.openai.com/api-keys)
#    - MAIL_USERNAME / MAIL_PASSWORD (Gmail App Password recommended)

# 4. Start all 10 containers
docker-compose up -d

# 5. Wait for services to be healthy (~60 seconds)
docker-compose ps

# 6. Access the application
#    Frontend:  http://localhost
#    Swagger:   http://localhost:8080/swagger-ui.html
#    RabbitMQ:  http://localhost:15672 (guest/guest)
```

### Verify Services

```bash
# Check all containers are running
docker-compose ps

# Check API health
curl http://localhost:8080/actuator/health

# View logs for a specific service
docker-compose logs -f api-gateway
docker-compose logs -f job-processor

# Stop everything
docker-compose down

# Stop and remove all data volumes
docker-compose down -v
```

---

## Local Development Setup

For active development, run infrastructure in Docker and services natively.

### Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| **Java** | 17 LTS | `sdk install java 17.0.10-tem` or [Adoptium](https://adoptium.net/) |
| **Maven** | 3.9+ | `sdk install maven` or [Apache Maven](https://maven.apache.org/) |
| **Node.js** | 18+ | `nvm install 18` or [Node.js](https://nodejs.org/) |
| **Python** | 3.11+ | `pyenv install 3.11` or [Python.org](https://python.org/) |
| **Docker** | 24+ | [Docker Desktop](https://docker.com/products/docker-desktop/) |

### Step 1: Start Infrastructure

```bash
# Start only MySQL, Redis, and RabbitMQ
docker-compose up -d mysql redis rabbitmq

# Verify they're healthy
docker-compose ps
# mysql       ... (healthy)
# redis       ... (healthy)
# rabbitmq    ... (healthy)
```

### Step 2: Configure Environment

```bash
cp .env.example .env
# Edit .env — set at least:
#   DB_USERNAME=aijobagent
#   DB_PASSWORD=your_secure_password
#   DB_ROOT_PASSWORD=root_password
#   JWT_SECRET=your_base64_secret_min_32_chars
#   OPENAI_API_KEY=sk-...
#   MAIL_USERNAME=your@gmail.com
#   MAIL_PASSWORD=your_app_password
```

### Step 3: Build and Run Backend

```bash
# Build all backend modules (from project root)
cd backend
mvn clean compile -q

# Run unit tests
mvn test -q

# Start each service in a separate terminal:

# Terminal 1 — API Gateway (port 8080)
cd backend/api-gateway
mvn spring-boot:run

# Terminal 2 — Job Processor (port 8081)
cd backend/job-processor
mvn spring-boot:run

# Terminal 3 — Document Service (port 8082)
cd backend/document-service
mvn spring-boot:run

# Terminal 4 — Notification Service (port 8083)
cd backend/notification-service
mvn spring-boot:run
```

### Step 4: Start Scraper Service

```bash
cd scraper-service

# Create virtual environment
python -m venv .venv
source .venv/bin/activate      # Linux/macOS
# .venv\Scripts\activate       # Windows

# Install dependencies
pip install -r requirements.txt

# Install Playwright browsers
playwright install chromium

# Run the service (port 8000)
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### Step 5: Start Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start dev server with hot reload (port 5173)
npm run dev

# Or build for production
npm run build
npm run preview    # Preview production build at port 4173
```

### Step 6: Access the Application

| URL | Service |
|-----|---------|
| http://localhost:5173 | Frontend (Vite dev server) |
| http://localhost:8080/swagger-ui.html | API Documentation |
| http://localhost:8080/actuator/health | Backend Health Check |
| http://localhost:8000/docs | Scraper API Docs (FastAPI) |
| http://localhost:15672 | RabbitMQ Management (guest/guest) |
| http://localhost:3306 | MySQL (use `mysql -h localhost -u <user> -p`) |

---

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_HOST` | No | `localhost` | MySQL hostname |
| `DB_PORT` | No | `3306` | MySQL port |
| `DB_NAME` | No | `aijobagent` | Database name |
| `DB_USERNAME` | **Yes** | — | MySQL username |
| `DB_PASSWORD` | **Yes** | — | MySQL password |
| `DB_ROOT_PASSWORD` | **Yes** | — | MySQL root password (Docker init) |
| `REDIS_HOST` | No | `localhost` | Redis hostname |
| `REDIS_PORT` | No | `6379` | Redis port |
| `REDIS_PASSWORD` | No | _(empty)_ | Redis password |
| `RABBITMQ_HOST` | No | `localhost` | RabbitMQ hostname |
| `RABBITMQ_PORT` | No | `5672` | RabbitMQ AMQP port |
| `RABBITMQ_USERNAME` | No | `guest` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | No | `guest` | RabbitMQ password |
| `JWT_SECRET` | **Yes** | — | HS256 signing key (min 256-bit, base64) |
| `OPENAI_API_KEY` | **Yes** | — | OpenAI API key (`sk-...`) |
| `AI_BASE_URL` | No | `https://api.openai.com/v1` | AI API base URL |
| `AI_ANALYSIS_MODEL` | No | `gpt-4o-mini` | Model for JD analysis |
| `AI_TAILOR_MODEL` | No | `gpt-4o` | Model for resume tailoring |
| `MAIL_HOST` | No | `smtp.gmail.com` | SMTP server |
| `MAIL_PORT` | No | `587` | SMTP port |
| `MAIL_USERNAME` | **Yes** | — | SMTP username |
| `MAIL_PASSWORD` | **Yes** | — | SMTP password / app password |
| `NOTIFICATION_FROM_EMAIL` | No | `noreply@aijobagent.dev` | Sender email address |
| `SCRAPE_CRON` | No | `0 0 6 * * *` | Job scraping schedule (6 AM) |
| `DAILY_DIGEST_CRON` | No | `0 0 7 * * *` | Daily digest schedule (7 AM) |
| `VITE_API_BASE_URL` | No | `http://localhost:8080` | Frontend → API base URL |
| `SWAGGER_ENABLED` | No | `true` | Enable/disable Swagger UI |

### Generating a JWT Secret

```bash
# Generate a secure 256-bit base64 key
openssl rand -base64 32
```

### Gmail App Password

For `MAIL_PASSWORD` with Gmail:
1. Go to https://myaccount.google.com/apppasswords
2. Select "Mail" and your device
3. Copy the 16-character app password
4. Use it as `MAIL_PASSWORD` (no spaces)

---

## Project Structure

```
ai-job-agent/
├── backend/
│   ├── pom.xml                        # Parent POM (multi-module)
│   ├── common/                        # Shared: entities, repos, DTOs, config, utils
│   │   └── src/main/java/.../
│   │       ├── config/                # Security, JWT, RabbitMQ, Redis, AI, OpenAPI
│   │       ├── entity/                # 17 JPA entities
│   │       ├── entity/enums/          # 9 enums (JobType, Status, etc.)
│   │       ├── repository/            # 18 Spring Data repositories
│   │       ├── dto/request/           # Request DTOs
│   │       ├── dto/response/          # Response DTOs
│   │       ├── exception/             # Custom exceptions + GlobalExceptionHandler
│   │       ├── security/              # JwtService, JwtAuthFilter
│   │       ├── service/               # OpenAiClient, AiUsageLogService
│   │       └── util/                  # SkillNormalizer, TextSanitizer
│   ├── api-gateway/                   # :8080 — REST API, auth, controllers
│   │   └── src/main/java/.../
│   │       ├── controller/            # 9 controllers (Auth, Job, Resume, etc.)
│   │       ├── service/               # AuthService, JobService, ResumeParserService
│   │       └── scheduler/             # ScrapeScheduler
│   ├── job-processor/                 # :8081 — Queue consumers: raw, analyze, match
│   │   └── src/main/java/.../
│   │       ├── messaging/consumer/    # JobRawConsumer, JobAnalyzeConsumer, JobMatchConsumer
│   │       └── service/               # JobPersistenceService, JobAnalysisService, MatchScoreService
│   ├── document-service/              # :8082 — Tailoring, cover letter, PDF
│   │   └── src/main/java/.../
│   │       ├── messaging/consumer/    # JobTailorConsumer
│   │       └── service/               # ResumeTailoringService, CoverLetterService, PdfGeneratorService
│   └── notification-service/          # :8083 — Email, daily digest, alerts
│       └── src/main/java/.../
│           ├── messaging/consumer/    # NotificationConsumer
│           ├── scheduler/             # DigestScheduler
│           └── service/               # EmailService, NotificationService, DailyDigestService
├── scraper-service/                   # Python/FastAPI scraper
│   ├── main.py                        # FastAPI app + Playwright scrapers
│   ├── requirements.txt               # Python dependencies
│   └── Dockerfile
├── frontend/                          # React 18 + Vite + Tailwind
│   ├── src/
│   │   ├── api/                       # API client modules (auth, jobs, resumes, etc.)
│   │   ├── components/                # Layout, ProtectedRoute
│   │   ├── components/ui/             # Reusable UI (Toast, Modal, ProgressBar, etc.)
│   │   ├── context/                   # AuthContext (JWT token management)
│   │   └── pages/                     # 9 pages (Dashboard, Matches, Applications, etc.)
│   ├── package.json
│   └── Dockerfile
├── database/
│   └── init.sql                       # MySQL schema (17 tables)
├── nginx/                             # Reverse proxy config
│   ├── nginx.conf
│   └── conf.d/default.conf
├── docker-compose.yml                 # Full stack orchestration (10 containers)
├── .env.example                       # Environment variable template
└── docs/                              # Full documentation suite
    ├── 01-BRD-Business-Requirements.md
    ├── 02-SRS-Software-Requirements.md
    ├── 03-PRD-Product-Requirements.md
    ├── 04-Project-Plan.md
    ├── 05-HLD-High-Level-Design.md
    ├── 06-LLD-Low-Level-Design.md
    ├── 07-Database-Schema.md
    ├── 08-API-Contracts.md
    ├── 09-Technical-Specifications.md
    ├── 10-Configuration.md
    ├── 11-Testing-Strategy.md
    ├── 12-Glossary.md
    └── 13-Project-Summary.md
```

---

## API Overview

All endpoints are prefixed with `/api/v1`. Protected endpoints require `Authorization: Bearer <token>`.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Create account |
| POST | `/auth/login` | Sign in, receive JWT |
| POST | `/auth/refresh` | Refresh access token |
| GET | `/auth/verify?token=` | Verify email |
| POST | `/auth/forgot-password` | Request password reset |
| POST | `/auth/reset-password` | Reset password |
| GET | `/users/profile` | Get user profile |
| PUT | `/users/profile` | Update profile |
| GET | `/users/preferences` | Get job preferences |
| PUT | `/users/preferences` | Update preferences |
| POST | `/resumes` | Upload resume (multipart) |
| GET | `/resumes` | List resumes |
| GET | `/resumes/{id}` | Get resume details |
| PUT | `/resumes/{id}/primary` | Set as primary |
| DELETE | `/resumes/{id}` | Delete resume |
| GET | `/jobs` | List jobs (paginated) |
| GET | `/jobs/{id}` | Get job details |
| GET | `/jobs/matches` | Get matched jobs with scores |
| POST | `/tailor/{jobId}` | Start resume tailoring (async) |
| GET | `/tailor/{id}` | Get tailored resume |
| GET | `/documents/resume/{id}` | Download tailored PDF |
| GET | `/documents/cover-letter/{id}` | Download cover letter PDF |
| GET | `/applications` | List applications |
| POST | `/applications` | Create application |
| PUT | `/applications/{id}` | Update application status |
| DELETE | `/applications/{id}` | Delete application |
| GET | `/notifications` | List notifications |
| PUT | `/notifications/{id}` | Mark as read |
| PUT | `/notifications/preferences` | Update notification settings |
| GET | `/admin/scrape-logs` | View scraping logs (admin) |
| GET | `/admin/ai-usage` | View AI usage stats (admin) |

Full API documentation with request/response examples is available at `/swagger-ui.html` when the api-gateway is running.

---

## Documentation

Comprehensive documentation is available in the [`docs/`](./docs/) directory:

| Document | Description |
|----------|-------------|
| [Business Requirements](docs/01-BRD-Business-Requirements.md) | Business objectives, KPIs, risk matrix |
| [Software Requirements](docs/02-SRS-Software-Requirements.md) | Functional & non-functional requirements |
| [Product Requirements](docs/03-PRD-Product-Requirements.md) | User stories, wireframes, MoSCoW priorities |
| [Project Plan](docs/04-Project-Plan.md) | 6-phase delivery plan, resource allocation |
| [High-Level Design](docs/05-HLD-High-Level-Design.md) | Architecture, tech stack, communication |
| [Low-Level Design](docs/06-LLD-Low-Level-Design.md) | Package structure, algorithms, security |
| [Database Schema](docs/07-Database-Schema.md) | 17 tables, ER diagram, indexes |
| [API Contracts](docs/08-API-Contracts.md) | REST endpoints, request/response examples |
| [Technical Specs](docs/09-Technical-Specifications.md) | AI prompts, match algorithm, retry strategy |
| [Configuration](docs/10-Configuration.md) | application.yml, Docker, environment vars |
| [Testing Strategy](docs/11-Testing-Strategy.md) | Test pyramid, 18 test cases, coverage targets |
| [Glossary](docs/12-Glossary.md) | Terms and acronyms |
| [Project Summary](docs/13-Project-Summary.md) | Executive overview |

---

## License

MIT
