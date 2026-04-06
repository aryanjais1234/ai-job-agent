# Business Requirements Document (BRD)
## AI Job Agent — Automated Job Application System
**Version:** 1.0  
**Date:** 2024-01-01  
**Author:** Aryan Jaiswal  
**Status:** Approved

---

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Business Objectives & KPIs](#business-objectives--kpis)
3. [Problem Statement](#problem-statement)
4. [Proposed Solution](#proposed-solution)
5. [Stakeholders](#stakeholders)
6. [Scope](#scope)
7. [Assumptions & Constraints](#assumptions--constraints)
8. [Risk Matrix](#risk-matrix)
9. [Success Criteria](#success-criteria)

---

## 1. Executive Summary

AI Job Agent is an intelligent, end-to-end automated job application platform that eliminates the repetitive manual effort of modern job searching. By combining web scraping, large language model (LLM) analysis, ATS-optimised resume tailoring, PDF generation, and multi-channel notifications, the system acts as a tireless personal career assistant.

The platform ingests job listings from major boards (Indeed, LinkedIn, Naukri), analyses each Job Description (JD) with an LLM to extract required skills and seniority signals, computes a match score against the candidate's uploaded resume, tailors the resume and cover letter for high-scoring matches, generates print-ready PDFs, and delivers a daily digest to the user. Every application is logged in a real-time dashboard for transparent tracking.

---

## 2. Business Objectives & KPIs

| # | Objective | KPI | Target |
|---|-----------|-----|--------|
| BO-1 | Reduce manual job-search time | Hours/week spent on manual search | ≤ 3 hrs (↓ 80%) |
| BO-2 | Increase interview conversion | Interview invites per 100 applications | 3× baseline |
| BO-3 | Maximise ATS pass-through rate | % of tailored resumes passing ATS screening | ≥ 95% |
| BO-4 | Scale job discovery | Unique jobs scraped and evaluated per day | ≥ 50 |
| BO-5 | Full application transparency | % of applications tracked end-to-end | 100% |
| BO-6 | Personalisation accuracy | User-rated relevance of job matches | ≥ 4.0 / 5.0 |
| BO-7 | Document quality | PDF generation success rate | ≥ 99.5% |

---

## 3. Problem Statement

### Current Pain Points

Modern job seekers face a fragmented, time-intensive process:

- **Volume Problem:** Hundreds of new postings per day across multiple platforms require constant manual monitoring.
- **Repetitive Tailoring:** Each application demands a customised resume and cover letter — a process taking 30–90 minutes per application.
- **ATS Black Box:** Applicant Tracking Systems reject up to 75% of resumes before human review due to keyword mismatches.
- **Tracking Chaos:** Job seekers maintain ad-hoc spreadsheets that quickly become stale and inaccurate.
- **Opportunity Loss:** High-quality matches are missed because the candidate simply did not see the posting in time.

**Quantified Impact:** A typical active job seeker spends 15–20 hours per week on job search activities, of which an estimated 60–70% is pure repetitive work that can be automated.

---

## 4. Proposed Solution

AI Job Agent implements a **multi-agent AI pipeline** with seven specialised agents:

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│  Scrape     │───▶│   Analyse    │───▶│    Match    │
│  Agent      │    │   Agent      │    │   Agent     │
└─────────────┘    └──────────────┘    └─────────────┘
                                              │
                                              ▼
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│  Notify     │◀───│   Generate   │◀───│   Tailor    │
│  Agent      │    │   Agent      │    │   Agent     │
└─────────────┘    └──────────────┘    └─────────────┘
                                              │
                                              ▼
                                       ┌─────────────┐
                                       │    Track    │
                                       │   Agent     │
                                       └─────────────┘
```

| Agent | Responsibility |
|-------|---------------|
| **Scrape Agent** | Crawls Indeed, LinkedIn, Naukri on a configurable schedule |
| **Analyse Agent** | Uses LLM to extract skills, experience bands, domain, seniority |
| **Match Agent** | Computes weighted match score between job and user resume |
| **Tailor Agent** | Rewrites resume bullets and skills section for the specific JD |
| **Generate Agent** | Renders tailored resume + cover letter to PDF |
| **Notify Agent** | Sends daily digest emails and real-time match alerts |
| **Track Agent** | Records application status and updates the dashboard |

---

## 5. Stakeholders

| Role | Name / Group | Interest | Influence |
|------|-------------|----------|-----------|
| Product Owner | Aryan Jaiswal | Define requirements, approve deliverables | High |
| Primary Users | Active Job Seekers | Reduce search time, increase interviews | High |
| Secondary Users | Passive Job Seekers | Opportunistic discovery | Medium |
| Development Team | Engineering | Technical implementation | High |
| AI / ML Team | Data Science | Prompt engineering, model tuning | High |
| DevOps | Infrastructure | Deployment, reliability | Medium |
| Legal / Compliance | Internal | Data privacy, ToS compliance | Medium |

---

## 6. Scope

### 6.1 In-Scope (v1.0)

- **Job Scraping:** Indeed, LinkedIn, Naukri via Playwright + HTTP
- **Resume Management:** Upload (PDF/DOCX), parse, store, manage multiple versions
- **AI JD Analysis:** Skill extraction, experience parsing, seniority classification
- **Match Scoring:** Weighted algorithm (Skill 50% + Experience 25% + Location 15% + Domain 10%)
- **Resume Tailoring:** LLM-driven personalisation for ATS optimisation
- **PDF Generation:** Tailored resume and cover letter rendering
- **Cover Letter Generation:** Personalised, tone-matched cover letters
- **Email Notifications:** Daily digest, match alerts, application status updates
- **Dashboard:** Application tracker, match history, analytics
- **User Authentication:** JWT-based auth, email verification

### 6.2 Out-of-Scope (v1.0)

| Feature | Reason Deferred |
|---------|----------------|
| Automated form submission / auto-apply | Legal risk, ToS violations on job boards |
| Mobile native app (iOS / Android) | Responsive web covers MVP; native is v2 |
| Payment / subscription billing | MVP is self-hosted; monetisation is v2 |
| Interview preparation assistant | Separate product domain |
| Salary negotiation coaching | Out of core job-search automation scope |
| Browser extension | UI scope for v2 |
| LinkedIn direct message outreach | High ToS violation risk |

---

## 7. Assumptions & Constraints

### 7.1 Assumptions

- Users have an existing resume in PDF or DOCX format.
- Target job boards (Indeed, LinkedIn, Naukri) remain publicly accessible for scraping.
- An OpenAI-compatible LLM API is available and funded.
- Users will review and approve tailored documents before submitting applications.
- SMTP credentials are available for email notification.

### 7.2 Constraints

| Type | Constraint |
|------|-----------|
| **Legal** | Must comply with GDPR; scraping must respect robots.txt and ToS |
| **Technical** | Java 17 / Spring Boot 3.2 mandated for backend; Python for scrapers |
| **Performance** | API responses ≤ 2 s (p95); pipeline completion ≤ 10 min/job |
| **Budget** | LLM API cost must remain < $0.05 per tailored application |
| **Security** | All PII encrypted at rest (AES-256) and in transit (TLS 1.3) |
| **Availability** | System uptime ≥ 99.5% during business hours |

---

## 8. Risk Matrix

| Risk ID | Risk | Likelihood | Impact | Severity | Mitigation |
|---------|------|-----------|--------|----------|-----------|
| R-01 | Job board blocks scraper IP | High | High | Critical | Rotate proxies, respect rate limits, Playwright stealth mode |
| R-02 | LLM API outage | Medium | High | High | Fallback to secondary provider (Anthropic), circuit breaker |
| R-03 | ATS algorithm changes | Medium | High | High | Continuous ATS testing, keyword density monitoring |
| R-04 | GDPR / data breach | Low | Critical | High | Encryption at rest, minimal data retention, audit logs |
| R-05 | Job board ToS change | Medium | Medium | Medium | Legal review quarterly; scraping only public data |
| R-06 | Resume parsing inaccuracy | Medium | Medium | Medium | Human-in-the-loop review step before submission |
| R-07 | Email deliverability issues | Low | Medium | Low | SPF/DKIM/DMARC setup, bounce monitoring |
| R-08 | Database performance degradation | Low | Medium | Low | Read replicas, connection pooling, query optimisation |

---

## 9. Success Criteria

The project is considered successful at v1.0 launch when:

1. ✅ System scrapes ≥ 50 unique job postings per day across all platforms
2. ✅ Resume tailoring completes within 5 minutes of job match detection
3. ✅ ATS simulation scores ≥ 85 for all generated resumes
4. ✅ PDF generation succeeds for ≥ 99.5% of tailored documents
5. ✅ Email notifications delivered within 15 minutes of trigger event
6. ✅ Dashboard reflects real-time application status
7. ✅ System handles 100 concurrent users without performance degradation
8. ✅ All PII encrypted and GDPR-compliant data handling documented
9. ✅ User onboarding completes in ≤ 5 minutes
10. ✅ Zero P0 security vulnerabilities at launch

---

*Document End — BRD v1.0*
