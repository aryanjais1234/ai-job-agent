# Project Plan
## AI Job Agent — Automated Job Application System
**Version:** 1.0  
**Total Duration:** 12 Weeks  
**Total Tasks:** 46  
**Methodology:** Agile (2-week sprints)

---

## Phase Overview

| Phase | Name | Weeks | Tasks | Deliverable |
|-------|------|-------|-------|-------------|
| 1 | Foundation | 1–2 | 8 | Running skeleton with auth, DB, CI/CD |
| 2 | Job Scraping | 3–4 | 8 | Automated daily scraping pipeline |
| 3 | AI Intelligence | 5–6 | 8 | JD analysis + match scoring |
| 4 | Document Generation | 7–8 | 6 | Tailored PDFs delivered to user |
| 5 | Dashboard & Notifications | 9–10 | 8 | Frontend dashboard + email notifications |
| 6 | Deploy & Polish | 11–12 | 8 | Production deployment + hardening |

---

## Phase 1: Foundation (Week 1–2)

**Goal:** Establish the project skeleton — version control, database, auth, and CI/CD pipeline.

| Task ID | Task | Owner | Est. Hours | Dependencies |
|---------|------|-------|-----------|--------------|
| T-1.1 | Initialise Git repository, branch strategy (main/develop/feature/*) | DevOps | 2 | — |
| T-1.2 | Bootstrap Spring Boot 3.2 project (pom.xml, package structure) | Backend | 4 | T-1.1 |
| T-1.3 | Bootstrap FastAPI scraper service (requirements.txt, main.py) | Backend | 3 | T-1.1 |
| T-1.4 | Bootstrap React 18 frontend (Vite, TailwindCSS, routing) | Frontend | 4 | T-1.1 |
| T-1.5 | Create MySQL schema (all 17 tables, indexes, FKs) — init.sql | Backend | 6 | T-1.2 |
| T-1.6 | Implement JWT authentication (register, login, refresh, verify email) | Backend | 8 | T-1.5 |
| T-1.7 | Set up Docker Compose (MySQL, Redis, RabbitMQ, all services) | DevOps | 4 | T-1.2, T-1.3, T-1.4 |
| T-1.8 | Configure GitHub Actions CI pipeline (build, test, lint) | DevOps | 3 | T-1.7 |

**Phase 1 Exit Criteria:**
- `docker-compose up` starts all services
- User can register, verify email, and obtain JWT token
- All 17 tables created with correct schema
- CI pipeline passes on every PR

---

## Phase 2: Job Scraping (Week 3–4)

**Goal:** Automated, scheduled, deduplicated job scraping from three platforms.

| Task ID | Task | Owner | Est. Hours | Dependencies |
|---------|------|-------|-----------|--------------|
| T-2.1 | Implement Playwright-based scraper for Indeed | Backend | 10 | T-1.3 |
| T-2.2 | Implement Playwright-based scraper for LinkedIn | Backend | 10 | T-1.3 |
| T-2.3 | Implement HTTP-based scraper for Naukri | Backend | 8 | T-1.3 |
| T-2.4 | Implement deduplication logic (URL + source_job_id hash) | Backend | 4 | T-2.1 |
| T-2.5 | Publish scraped jobs to RabbitMQ `jobs.raw` queue | Backend | 3 | T-2.4 |
| T-2.6 | Implement Spring Boot consumer for `jobs.raw` queue (persist to DB) | Backend | 4 | T-2.5 |
| T-2.7 | Configure cron scheduler (default: 6 AM IST daily) | Backend | 2 | T-2.6 |
| T-2.8 | Implement scrape_logs table writer + admin view | Backend | 3 | T-2.6 |

**Phase 2 Exit Criteria:**
- ≥ 50 unique jobs scraped on first run
- Zero duplicate entries in `jobs` table
- Scrape logs visible in admin endpoint

---

## Phase 3: AI Intelligence (Week 5–6)

**Goal:** LLM-powered JD analysis and weighted match scoring engine.

| Task ID | Task | Owner | Est. Hours | Dependencies |
|---------|------|-------|-----------|--------------|
| T-3.1 | Implement OpenAI client wrapper with retry + circuit breaker | Backend | 6 | T-1.2 |
| T-3.2 | Build JD Analysis prompt + response parser (skills, exp, seniority) | AI/ML | 8 | T-3.1 |
| T-3.3 | Persist analysis results to `job_analyses` and `job_skills` tables | Backend | 4 | T-3.2 |
| T-3.4 | Implement resume parsing service (Apache Tika / PDFBox) | Backend | 8 | T-1.5 |
| T-3.5 | Populate `resume_skills`, `resume_experiences`, `resume_educations` | Backend | 4 | T-3.4 |
| T-3.6 | Implement Match Score Algorithm (Skill 50% + Exp 25% + Location 15% + Domain 10%) | Backend | 8 | T-3.3, T-3.5 |
| T-3.7 | Persist match results to `job_matches` table | Backend | 2 | T-3.6 |
| T-3.8 | Log AI token usage and cost to `ai_usage_logs` | Backend | 2 | T-3.2 |

**Phase 3 Exit Criteria:**
- JD analysis completes in < 30 seconds per job
- Match score computed for all user-job pairs above threshold
- AI cost per analysis < $0.02

---

## Phase 4: Document Generation (Week 7–8)

**Goal:** AI-powered resume tailoring + PDF generation for top matches.

| Task ID | Task | Owner | Est. Hours | Dependencies |
|---------|------|-------|-----------|--------------|
| T-4.1 | Build resume tailoring prompt (rewrite bullets, reorder skills, add keywords) | AI/ML | 10 | T-3.6 |
| T-4.2 | Persist tailored content + modifications log to `tailored_resumes` | Backend | 4 | T-4.1 |
| T-4.3 | Implement OpenPDF-based PDF renderer for tailored resume | Backend | 8 | T-4.2 |
| T-4.4 | Build cover letter generation prompt + parser | AI/ML | 8 | T-4.1 |
| T-4.5 | Implement PDF renderer for cover letter | Backend | 4 | T-4.4 |
| T-4.6 | Expose download endpoints for both PDFs; log to `applications` table | Backend | 4 | T-4.3, T-4.5 |

**Phase 4 Exit Criteria:**
- Tailored resume PDF downloadable within 5 minutes of match detection
- All modifications logged and visible
- ATS simulation score ≥ 85 for test resumes

---

## Phase 5: Dashboard & Notifications (Week 9–10)

**Goal:** React dashboard with live tracker + email notification system.

| Task ID | Task | Owner | Est. Hours | Dependencies |
|---------|------|-------|-----------|--------------|
| T-5.1 | Build Login / Registration screens (React + JWT flow) | Frontend | 8 | T-1.6 |
| T-5.2 | Build Onboarding wizard (resume upload + preferences) | Frontend | 8 | T-3.4 |
| T-5.3 | Build Dashboard (job cards, match scores, filter/sort) | Frontend | 10 | T-3.6 |
| T-5.4 | Build Resume Preview & Tailoring screen | Frontend | 8 | T-4.2 |
| T-5.5 | Build Application Tracker screen | Frontend | 8 | T-4.6 |
| T-5.6 | Implement Spring Mail daily digest email (top 5 matches) | Backend | 6 | T-3.6 |
| T-5.7 | Implement real-time match alert email | Backend | 4 | T-3.6 |
| T-5.8 | Implement notification preferences management | Backend | 4 | T-5.6 |

**Phase 5 Exit Criteria:**
- Full user journey works end-to-end in browser
- Daily digest email received with correct matches
- Application tracker updates reflect database state

---

## Phase 6: Deploy & Polish (Week 11–12)

**Goal:** Production-ready deployment with monitoring, security hardening, and documentation.

| Task ID | Task | Owner | Est. Hours | Dependencies |
|---------|------|-------|-----------|--------------|
| T-6.1 | Production Docker images (multi-stage builds, non-root users) | DevOps | 6 | All |
| T-6.2 | Set up Nginx reverse proxy with TLS termination | DevOps | 4 | T-6.1 |
| T-6.3 | Deploy to cloud VM (Ubuntu 22.04, Docker Compose) | DevOps | 4 | T-6.2 |
| T-6.4 | Configure MySQL backups (daily dumps to object storage) | DevOps | 3 | T-6.3 |
| T-6.5 | Implement rate limiting on all public endpoints | Backend | 4 | T-6.3 |
| T-6.6 | Security audit: SQL injection, XSS, CSRF, JWT hardening | Security | 8 | T-6.5 |
| T-6.7 | Performance test: 100 concurrent users (k6 or JMeter) | QA | 6 | T-6.5 |
| T-6.8 | Write integration tests (≥ 80% coverage); update all docs | QA/Dev | 8 | All |

**Phase 6 Exit Criteria:**
- System live and accessible via HTTPS
- All 18 test cases passing (see Testing Strategy doc)
- Zero P0/P1 security findings
- Documentation complete and reviewed

---

## Gantt Chart (Text-Based)

```
Week:     1    2    3    4    5    6    7    8    9    10   11   12
          ─────────────────────────────────────────────────────────
Phase 1:  ████ ████
Phase 2:            ████ ████
Phase 3:                      ████ ████
Phase 4:                                ████ ████
Phase 5:                                          ████ ████
Phase 6:                                                    ████ ████
```

---

## Resource Allocation

| Role | Phase 1 | Phase 2 | Phase 3 | Phase 4 | Phase 5 | Phase 6 |
|------|---------|---------|---------|---------|---------|---------|
| Backend Engineer | 70% | 80% | 60% | 70% | 40% | 30% |
| AI/ML Engineer | 10% | 10% | 80% | 60% | 10% | 10% |
| Frontend Engineer | 30% | 10% | 10% | 10% | 90% | 20% |
| DevOps Engineer | 50% | 10% | 10% | 10% | 10% | 70% |
| QA Engineer | 10% | 20% | 20% | 30% | 30% | 80% |

---

*Document End — Project Plan v1.0*
