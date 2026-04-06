# High-Level Design (HLD)
## AI Job Agent — Automated Job Application System
**Version:** 1.0  
**Date:** 2024-01-01  
**Author:** Aryan Jaiswal

---

## Table of Contents
1. [Architecture Style](#architecture-style)
2. [System Architecture Diagram](#system-architecture-diagram)
3. [Component Descriptions](#component-descriptions)
4. [Communication Patterns](#communication-patterns)
5. [Multi-Agent Pipeline Flow](#multi-agent-pipeline-flow)
6. [Technology Stack](#technology-stack)
7. [Deployment Architecture](#deployment-architecture)

---

## 1. Architecture Style

AI Job Agent is built on a **Microservices with Event-Driven Async** architecture:

- **API Gateway Pattern** — All client requests enter through the Spring Boot API gateway layer which handles auth, rate limiting, and routing.
- **Event-Driven Pipeline** — The job processing pipeline uses RabbitMQ message queues for decoupling scraping, analysis, matching, and generation steps.
- **Stateless Services** — All services are stateless; session state is stored in Redis; file state in object storage.
- **Database per Service Concept** — Although sharing a single MySQL instance for v1, each service accesses only its own table domain.

---

## 2. System Architecture Diagram

```
                              ┌──────────────────────────────┐
                              │         CLIENTS               │
                              │  Browser (React 18 + Vite)   │
                              └──────────────┬───────────────┘
                                             │ HTTPS / REST
                              ┌──────────────▼───────────────┐
                              │        NGINX (Reverse Proxy)  │
                              │       TLS Termination         │
                              └──────┬────────────┬──────────┘
                                     │            │
                        ┌────────────▼──┐   ┌────▼────────────────┐
                        │  Spring Boot  │   │   FastAPI Scraper    │
                        │  Backend      │   │   Service            │
                        │  :8080        │   │   :8000              │
                        │               │   │                      │
                        │  • Auth       │   │  • Indeed Scraper    │
                        │  • Resume API │   │  • LinkedIn Scraper  │
                        │  • Match API  │   │  • Naukri Scraper    │
                        │  • Doc API    │   │  • Playwright        │
                        │  • Notify API │   └──────────┬──────────┘
                        │  • Track API  │              │
                        └──────┬────────┘              │
                               │                       │
          ┌────────────────────┼───────────────────────┘
          │                    │
          ▼                    ▼
┌─────────────────┐   ┌─────────────────────────────────────┐
│     MySQL 8     │   │          RabbitMQ 3.x               │
│   (Primary DB)  │   │                                     │
│                 │   │  Exchanges & Queues:                 │
│  • users        │   │  ├── jobs.raw          (scraper →)  │
│  • resumes      │   │  ├── jobs.analyze      (→ AI)       │
│  • jobs         │   │  ├── jobs.match        (→ scorer)   │
│  • job_matches  │   │  ├── jobs.tailor       (→ LLM)      │
│  • applications │   │  ├── docs.generate     (→ PDF)      │
│  • ...          │   │  └── notify.send       (→ mail)     │
└─────────────────┘   └─────────────────────────────────────┘
          ▲                    │
          │                    ▼
┌─────────────────┐   ┌─────────────────────────────────────┐
│   Redis Cache   │   │          OpenAI API                 │
│                 │   │   (GPT-4o / GPT-4o-mini)            │
│  • JWT tokens   │   │                                     │
│  • Rate limits  │   │  Used for:                          │
│  • Session data │   │  • JD Analysis                      │
│  • Job cache    │   │  • Resume Tailoring                 │
└─────────────────┘   │  • Cover Letter Generation          │
                      └─────────────────────────────────────┘
```

---

## 3. Component Descriptions

### 3.1 Spring Boot Backend Service

The core business logic service. Responsibilities:
- **Authentication Module:** JWT issue/validate, email verification, password reset
- **Resume Module:** Upload, parse (Apache Tika), store, expose for review
- **Job Module:** Receive from RabbitMQ, persist, expose via REST
- **Analysis Module:** Consume `jobs.analyze` queue, call OpenAI, persist results
- **Match Module:** Compute scores, filter, persist to `job_matches`
- **Tailoring Module:** Consume `jobs.tailor` queue, call OpenAI, persist tailored content
- **PDF Module:** Use OpenPDF to generate resume and cover letter PDFs
- **Notification Module:** Spring Mail + SMTP, process `notify.send` queue
- **Tracking Module:** CRUD for application status updates

### 3.2 FastAPI Scraper Service

A dedicated Python service for web scraping. Responsibilities:
- Scheduled or on-demand scraping of Indeed, LinkedIn, Naukri
- Playwright for JavaScript-rendered pages; httpx for REST APIs
- Normalise job data into a standard schema
- Publish to RabbitMQ `jobs.raw` queue
- Self-contained — exposes `/health` and `/scrape/{platform}` endpoints

### 3.3 React Frontend

Single-page application. Responsibilities:
- Authentication flow (login, register, email verification)
- Onboarding wizard (resume upload + preferences)
- Dashboard with job cards and match scores
- Resume tailoring review screen
- Application tracker with status management
- Notification preferences settings

### 3.4 MySQL 8.0

Relational database for all persistent data. 17 tables covering users, resumes, jobs, analyses, matches, applications, notifications, and audit logs.

### 3.5 RabbitMQ

Message broker for decoupling the pipeline stages. Enables:
- Async processing — scraper is independent of backend
- Retry / DLQ — failed messages requeued with exponential backoff
- Scalability — add more consumers without changing producers

### 3.6 Redis

In-memory data store used for:
- JWT token blocklist (logout / revocation)
- Rate limiting counters
- Caching frequently-read job data

---

## 4. Communication Patterns

| From | To | Protocol | Pattern |
|------|----|----------|---------|
| Browser | Nginx | HTTPS REST | Request/Response |
| Nginx | Spring Boot | HTTP REST | Request/Response |
| Nginx | FastAPI | HTTP REST | Request/Response |
| FastAPI Scraper | RabbitMQ | AMQP | Publish |
| Spring Boot | RabbitMQ | AMQP | Publish / Subscribe |
| Spring Boot | MySQL | JDBC | Query/Response |
| Spring Boot | Redis | Redis Protocol | Get/Set |
| Spring Boot | OpenAI API | HTTPS REST | Request/Response |
| Spring Boot | SMTP Server | SMTP | Fire-and-forget |

---

## 5. Multi-Agent Pipeline Flow

```
┌─────────────┐
│   Scraper   │  Runs at 6 AM IST daily (cron)
│   Agent     │  Scrapes Indeed + LinkedIn + Naukri
└──────┬──────┘
       │  Publishes to jobs.raw
       ▼
┌─────────────┐
│   Persist   │  Consumer deduplicates by source_url
│   Consumer  │  Saves new jobs to `jobs` table
└──────┬──────┘
       │  Publishes to jobs.analyze
       ▼
┌─────────────┐
│   Analyse   │  Calls OpenAI GPT-4o-mini
│   Agent     │  Extracts: skills, exp, seniority, domain
└──────┬──────┘
       │  Saves to job_analyses; publishes to jobs.match
       ▼
┌─────────────┐
│    Match    │  For each active user with preferences:
│   Agent     │  Computes weighted score (Skill/Exp/Loc/Domain)
└──────┬──────┘
       │  Saves to job_matches (score ≥ 70 only triggers next step)
       ▼
┌─────────────┐
│   Tailor    │  Calls OpenAI GPT-4o
│   Agent     │  Rewrites resume for JD keywords + ATS
└──────┬──────┘
       │  Saves tailored content + mod log; publishes to docs.generate
       ▼
┌─────────────┐
│  Generate   │  Uses OpenPDF to render tailored resume
│   Agent     │  Generates cover letter PDF
└──────┬──────┘
       │  Saves PDF paths; publishes to notify.send
       ▼
┌─────────────┐
│   Notify    │  Sends real-time match alert email
│   Agent     │  Batches for daily digest
└──────┬──────┘
       │  Logs to notification_logs
       ▼
┌─────────────┐
│    Track    │  Creates entry in `applications` table
│   Agent     │  User can update status via dashboard
└─────────────┘
```

**Total pipeline latency target:** ≤ 10 minutes from scrape to notification

---

## 6. Technology Stack

| Layer | Technology | Version | Justification |
|-------|-----------|---------|---------------|
| Backend Framework | Spring Boot | 3.2.x | Industry standard; robust ecosystem; Java 17 LTS |
| Backend Language | Java | 17 LTS | LTS release; strong typing; enterprise libraries |
| Scraper Framework | FastAPI | 0.110+ | Async Python; lightweight; OpenAPI auto-docs |
| Scraper Language | Python | 3.11+ | Best ecosystem for web scraping and AI libraries |
| Browser Automation | Playwright | 1.40+ | Handles JS-rendered pages; stealth mode support |
| Frontend Framework | React | 18.x | Component model; large ecosystem; virtual DOM |
| Frontend Build | Vite | 5.x | Fast HMR; ESM-native; better than CRA |
| CSS Framework | TailwindCSS | 3.x | Utility-first; consistent design system |
| Database | MySQL | 8.0 | ACID compliant; InnoDB; wide hosting support |
| ORM | Spring Data JPA / Hibernate | — | Type-safe queries; migration support |
| Message Broker | RabbitMQ | 3.12+ | AMQP protocol; DLQ support; management UI |
| Cache | Redis | 7.x | Sub-millisecond reads; pub/sub; atomic ops |
| AI Provider | OpenAI | GPT-4o / GPT-4o-mini | Best-in-class reasoning; structured outputs |
| PDF Generation | OpenPDF | 1.3.x | Open-source iText fork; no AGPL restrictions |
| Security | Spring Security + JJWT | 0.12.x | Proven JWT library; Spring integration |
| API Docs | Springdoc OpenAPI | 2.x | Swagger UI auto-generated from annotations |
| Containerisation | Docker | 24.x | Standard; docker-compose for local dev |
| Reverse Proxy | Nginx | 1.25+ | High-performance; TLS termination; load balance |
| CI/CD | GitHub Actions | — | Native GitHub integration; free for OSS |

---

## 7. Deployment Architecture

```
                        Internet
                           │
                    ┌──────▼──────┐
                    │  Cloud VM   │
                    │ Ubuntu 22.04│
                    │  (8 GB RAM) │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │    Nginx    │  :80 → :443 redirect
                    │ (TLS 1.3)   │  :443 → backend/frontend
                    └──┬──────┬───┘
                       │      │
              ┌────────▼──┐  ┌▼──────────────┐
              │  Frontend │  │  Backend API  │
              │ React SPA │  │ Spring Boot   │
              │  :3000    │  │   :8080       │
              └───────────┘  └──────┬────────┘
                                    │
                   ┌────────────────┼──────────────┐
                   │                │              │
          ┌────────▼──┐   ┌─────────▼──┐  ┌───────▼───┐
          │  MySQL 8  │   │  RabbitMQ  │  │   Redis   │
          │   :3306   │   │   :5672    │  │   :6379   │
          └───────────┘   └────────────┘  └───────────┘
                   │
          ┌────────▼──────┐
          │ FastAPI Scraper│
          │    :8000      │
          └───────────────┘
```

**Data Persistence:**
- MySQL: Docker volume `mysql_data`
- Redis: Docker volume `redis_data`
- PDFs: Docker volume `pdf_storage`
- Uploaded resumes: Docker volume `resume_storage`

---

*Document End — HLD v1.0*
