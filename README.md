# 🧠 AI Job Agent

> **Automated, end-to-end job application assistant powered by LLMs**

AI Job Agent eliminates the 15–20 hours per week job seekers waste on manual searching, resume tailoring, and application tracking. It scrapes jobs from Indeed, LinkedIn, and Naukri; analyses each JD with GPT-4o; tailors your resume for ATS; generates professional PDFs; and delivers daily match digests — all automatically.

---

## Architecture

```
Browser (React 18)
       │ HTTPS
       ▼
   Nginx (TLS Termination)
   ┌────────┴────────────────────┐
   ▼                             ▼
Spring Boot :8080          FastAPI Scraper :8000
   │                             │
   ├── MySQL 8.0                 └── Playwright (Indeed / LinkedIn / Naukri)
   ├── Redis 7 (cache / JWT)              │
   └── RabbitMQ 3.12 ◄───────────────────┘
            │
      AI Pipeline Queues
      ├── jobs.raw → jobs.analyze → jobs.match
      └── jobs.tailor → docs.generate → notify.send
            │
       OpenAI API (GPT-4o / GPT-4o-mini)
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17 + Spring Boot 3.2 |
| Scraper | Python 3.11 + FastAPI + Playwright |
| Frontend | React 18 + TailwindCSS + Vite |
| Database | MySQL 8.0 (17 tables) |
| Cache | Redis 7 |
| Message Queue | RabbitMQ 3.12 |
| AI | OpenAI GPT-4o / GPT-4o-mini |
| PDF | OpenPDF |
| Auth | Spring Security + JJWT 0.12 |
| Docs | Springdoc OpenAPI 2 |

---

## Features

- [x] 🔍 **Multi-platform scraping** — Indeed, LinkedIn, Naukri (50+ jobs/day)
- [x] 🧠 **LLM JD analysis** — skills, seniority, domain, ATS keywords
- [x] 🎯 **Weighted match scoring** — Skill 50% + Experience 25% + Location 15% + Domain 10%
- [x] ✏️ **AI resume tailoring** — GPT-4o rewrites bullets, adds keywords naturally
- [x] 📝 **Cover letter generation** — tone-matched to role seniority
- [x] 📄 **PDF generation** — print-ready resume and cover letter
- [x] 📧 **Email notifications** — daily digest + real-time high-score alerts
- [x] 📊 **Application tracker** — Pending → Applied → Interview → Offer pipeline
- [x] 🔒 **JWT auth** — BCrypt passwords, TLS 1.3, AES-256 at rest
- [x] 🐳 **Docker Compose** — one-command local setup

---

## Quick Start

```bash
# 1. Clone
git clone https://github.com/aryanjais1234/ai-job-agent.git
cd ai-job-agent

# 2. Configure
cp .env.example .env
# Edit .env — set OPENAI_API_KEY, DB_PASSWORD, JWT_SECRET, MAIL_* credentials

# 3. Start all services
docker-compose up -d

# 4. Access
#  Frontend:   http://localhost:3000
#  API:        http://localhost:8080/api/v1
#  RabbitMQ:   http://localhost:15672  (guest/guest by default)
```

---

## Documentation

| # | Document | Description |
|---|----------|-------------|
| 01 | [Business Requirements](docs/01-BRD-Business-Requirements.md) | Objectives, KPIs, scope, risk matrix |
| 02 | [Software Requirements](docs/02-SRS-Software-Requirements.md) | 9 functional modules, 15 NFRs |
| 03 | [Product Requirements](docs/03-PRD-Product-Requirements.md) | MoSCoW, 10 user stories, UI wireframes |
| 04 | [Project Plan](docs/04-Project-Plan.md) | 6-phase plan, 46 tasks, Gantt |
| 05 | [High-Level Design](docs/05-HLD-High-Level-Design.md) | Architecture, components, tech stack |
| 06 | [Low-Level Design](docs/06-LLD-Low-Level-Design.md) | Package structure, APIs, class diagrams |
| 07 | [Database Schema](docs/07-Database-Schema.md) | ER diagram, all 17 tables |
| 08 | [API Contracts](docs/08-API-Contracts.md) | Request/response examples |
| 09 | [Technical Specifications](docs/09-Technical-Specifications.md) | AI prompts, algorithms, security |
| 10 | [Configuration](docs/10-Configuration.md) | application.yml, docker-compose.yml, env vars |
| 11 | [Testing Strategy](docs/11-Testing-Strategy.md) | Test pyramid, 18 test cases |
| 12 | [Glossary](docs/12-Glossary.md) | All technical terms defined |
| 13 | [Project Summary](docs/13-Project-Summary.md) | Shareable one-pager |

---

## Project Phases

| Phase | Scope | Timeline |
|-------|-------|----------|
| 1 — Foundation | Auth, DB schema, Docker, CI/CD | Week 1–2 |
| 2 — Job Scraping | Indeed + LinkedIn + Naukri + RabbitMQ | Week 3–4 |
| 3 — AI Intelligence | JD analysis + match scoring | Week 5–6 |
| 4 — Document Generation | Resume tailoring + PDF generation | Week 7–8 |
| 5 — Dashboard & Notifications | React UI + email notifications | Week 9–10 |
| 6 — Deploy & Polish | Production hardening + testing | Week 11–12 |

---

## License

[MIT License](LICENSE) — © 2024 Aryan Jaiswal
