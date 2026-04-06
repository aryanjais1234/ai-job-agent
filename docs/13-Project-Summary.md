# Project Summary
## AI Job Agent — Automated Job Application System

**Author:** Aryan Jaiswal  
**Version:** 1.0  
**Status:** In Development  
**Repository:** [github.com/aryanjais1234/ai-job-agent](https://github.com/aryanjais1234/ai-job-agent)

---

## What Is AI Job Agent?

**AI Job Agent** is a full-stack, AI-powered job application automation platform. It acts as a tireless personal career assistant: it automatically discovers relevant job postings, analyses them with large language models, tailors your resume and cover letter for each position, generates professional PDFs, and tracks every application — all without you having to visit a single job board.

---

## The Problem It Solves

Modern job searching is broken:
- **Time sink:** Active job seekers spend 15–20 hours per week manually searching, tailoring, and applying
- **ATS rejection:** Up to 75% of resumes are filtered out before human review due to keyword mismatches
- **Opportunity loss:** Relevant postings expire before candidates discover them
- **Tracking chaos:** Ad-hoc spreadsheets miss follow-ups and lose historical context

---

## How It Works

```
Daily at 6 AM → Scrape Indeed, LinkedIn, Naukri (50+ jobs/day)
              ↓
         LLM analyses each JD (skills, seniority, keywords)
              ↓
    Weighted match score computed per user (Skill 50% + Exp 25% + Location 15% + Domain 10%)
              ↓
  For matches ≥ 75%: LLM tailors resume + generates cover letter
              ↓
         PDF generated; user notified by email
              ↓
       User reviews, downloads, applies — dashboard updated
```

---

## Key Features

| Feature | Detail |
|---------|--------|
| 🔍 Multi-Platform Scraping | Indeed, LinkedIn, Naukri — deduplicated |
| 🧠 LLM JD Analysis | GPT-4o-mini extracts skills, seniority, domain, ATS keywords |
| 🎯 Smart Match Scoring | Weighted algorithm with 4 components |
| ✏️ AI Resume Tailoring | GPT-4o rewrites bullets, reorders skills, adds keywords |
| 📝 Cover Letter Generation | Personalised, tone-matched to role seniority |
| 📄 PDF Generation | OpenPDF renders print-ready documents |
| 📧 Email Notifications | Daily digest + real-time high-score alerts |
| 📊 Application Tracker | Status pipeline: Pending → Applied → Interview → Offer |
| 🔒 Security | JWT auth, BCrypt passwords, TLS 1.3, AES-256 at rest |

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17 + Spring Boot 3.2 |
| Scraper | Python 3.11 + FastAPI + Playwright |
| Frontend | React 18 + TailwindCSS + Vite |
| Database | MySQL 8.0 (17 tables) |
| Cache | Redis 7 |
| Queue | RabbitMQ 3.12 |
| AI | OpenAI GPT-4o / GPT-4o-mini |
| PDF | OpenPDF |
| Auth | Spring Security + JJWT |
| Deploy | Docker Compose + Nginx |

---

## Project Scale

| Metric | Value |
|--------|-------|
| Documentation files | 13 |
| Database tables | 17 |
| API endpoints | 30+ |
| Test cases | 18 |
| Project phases | 6 |
| Planned tasks | 46 |
| Timeline | 12 weeks |

---

## Business Impact

| KPI | Target |
|-----|--------|
| Manual search time reduction | 80% (15–20 hrs → ≤ 3 hrs/week) |
| Interview conversion improvement | 3× baseline |
| ATS pass rate | ≥ 95% |
| Jobs scraped per day | ≥ 50 |
| Application tracking coverage | 100% |

---

## Documentation Index

| Doc | Description |
|-----|-------------|
| [01-BRD](01-BRD-Business-Requirements.md) | Business objectives, stakeholders, scope, risk matrix |
| [02-SRS](02-SRS-Software-Requirements.md) | 9 functional modules (FR-101 to FR-905), 15 NFRs |
| [03-PRD](03-PRD-Product-Requirements.md) | MoSCoW priorities, 10 user stories, 5 UI wireframes |
| [04-Project-Plan](04-Project-Plan.md) | 6-phase plan, 46 tasks, Gantt chart |
| [05-HLD](05-HLD-High-Level-Design.md) | Architecture diagram, component descriptions, tech stack |
| [06-LLD](06-LLD-Low-Level-Design.md) | Package structure, APIs, class/sequence diagrams |
| [07-Database-Schema](07-Database-Schema.md) | ER diagram, all 17 table definitions |
| [08-API-Contracts](08-API-Contracts.md) | Request/response examples for all endpoints |
| [09-Technical-Specifications](09-Technical-Specifications.md) | AI prompts, match algorithm, security design |
| [10-Configuration](10-Configuration.md) | application.yml, docker-compose.yml, env vars |
| [11-Testing-Strategy](11-Testing-Strategy.md) | Test pyramid, 18 test cases |
| [12-Glossary](12-Glossary.md) | All technical terms defined |

---

## Quick Start

```bash
# Clone repository
git clone https://github.com/aryanjais1234/ai-job-agent.git
cd ai-job-agent

# Configure environment
cp .env.example .env
# Edit .env with your API keys and credentials

# Start all services
docker-compose up -d

# Access
# Frontend:  http://localhost:3000
# API:       http://localhost:8080/api/v1
# Swagger:   http://localhost:8080/swagger-ui.html (dev only)
# RabbitMQ:  http://localhost:15672
```

---

## License

MIT License — see [LICENSE](../LICENSE) for details.

---

*Document End — Project Summary v1.0*
