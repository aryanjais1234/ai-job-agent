# Software Requirements Specification (SRS)
## AI Job Agent — Automated Job Application System
**Version:** 1.0  
**Date:** 2024-01-01  
**Author:** Aryan Jaiswal  
**Status:** Approved

---

## Table of Contents
1. [User Personas](#user-personas)
2. [Functional Requirements](#functional-requirements)
3. [Non-Functional Requirements](#non-functional-requirements)

---

## 1. User Personas

### Persona A — Active Job Seeker

| Attribute | Detail |
|-----------|--------|
| **Name** | Priya Sharma |
| **Age** | 27 |
| **Role** | Software Engineer (3 yrs exp) |
| **Goal** | Land a senior role in 60 days |
| **Pain Points** | Spending evenings tailoring resumes; losing track of applications |
| **Tech Comfort** | High — uses GitHub, VS Code, cloud tools |
| **Usage Pattern** | Daily — checks dashboard every morning; reviews matches at lunch |

### Persona B — Passive Job Seeker

| Attribute | Detail |
|-----------|--------|
| **Name** | Rahul Mehta |
| **Age** | 34 |
| **Role** | Product Manager |
| **Goal** | Open to exceptional opportunities while employed |
| **Pain Points** | No time for active searching; misses relevant postings |
| **Tech Comfort** | Medium — comfortable with web apps |
| **Usage Pattern** | Weekly — checks weekly digest email; approves applications selectively |

---

## 2. Functional Requirements

### Module 1: User Management (FR-1xx)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-101 | System shall allow users to register with email and password | Must Have |
| FR-102 | System shall send email verification on registration | Must Have |
| FR-103 | System shall authenticate users using JWT tokens with 24h expiry | Must Have |
| FR-104 | System shall support password reset via email OTP | Must Have |
| FR-105 | System shall allow users to update profile (name, phone, LinkedIn, GitHub, portfolio, location, years of experience) | Must Have |
| FR-106 | System shall enforce role-based access: USER, ADMIN | Should Have |
| FR-107 | System shall log all authentication events for audit | Should Have |
| FR-108 | System shall support account deactivation (soft delete) | Should Have |

---

### Module 2: Resume Management (FR-2xx)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-201 | System shall accept resume uploads in PDF and DOCX formats | Must Have |
| FR-202 | System shall extract text from uploaded resumes using Apache Tika or PDFBox | Must Have |
| FR-203 | System shall parse and store: skills, work experience entries, education, contact info | Must Have |
| FR-204 | System shall allow a user to upload multiple resume versions | Must Have |
| FR-205 | System shall allow a user to set one resume as primary | Must Have |
| FR-206 | System shall display parsed resume data for user verification | Must Have |
| FR-207 | System shall reject files exceeding 10 MB | Must Have |
| FR-208 | System shall store original uploaded file securely in object storage | Should Have |
| FR-209 | System shall display skill categories: Technical, Soft, Domain | Could Have |

---

### Module 3: Job Preference Management (FR-3xx)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-301 | System shall allow users to set target job titles (multiple) | Must Have |
| FR-302 | System shall allow users to set preferred locations (multiple) | Must Have |
| FR-303 | System shall allow users to set salary range (min/max) | Must Have |
| FR-304 | System shall allow users to specify job types: Full-time, Part-time, Contract, Internship | Must Have |
| FR-305 | System shall allow users to set remote preference: Remote, Hybrid, On-site | Must Have |
| FR-306 | System shall allow users to set preferred experience level | Should Have |
| FR-307 | System shall allow users to blacklist specific companies | Could Have |
| FR-308 | System shall allow users to filter by industry/domain | Could Have |

---

### Module 4: Job Scraping (FR-4xx)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-401 | System shall scrape job listings from Indeed daily | Must Have |
| FR-402 | System shall scrape job listings from LinkedIn daily | Must Have |
| FR-403 | System shall scrape job listings from Naukri daily | Must Have |
| FR-404 | System shall deduplicate jobs by source URL and job ID | Must Have |
| FR-405 | System shall store: title, company, location, description, requirements, salary (if available), posted date | Must Have |
| FR-406 | System shall run scraping on a configurable cron schedule (default: 6 AM IST) | Must Have |
| FR-407 | System shall log scraping results: jobs found, new, updated, errors | Must Have |
| FR-408 | System shall mark expired/removed jobs as inactive | Should Have |

---

### Module 5: AI Job Analysis (FR-5xx)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-501 | System shall extract required skills from JD using LLM | Must Have |
| FR-502 | System shall extract nice-to-have skills from JD using LLM | Must Have |
| FR-503 | System shall extract minimum and maximum years of experience from JD | Must Have |
| FR-504 | System shall classify seniority level: Junior, Mid, Senior, Lead, Manager | Must Have |
| FR-505 | System shall identify remote type: Remote, Hybrid, On-site | Must Have |
| FR-506 | System shall classify domain/industry from JD | Must Have |
| FR-507 | System shall extract education requirements from JD | Should Have |
| FR-508 | System shall extract ATS keywords from JD | Should Have |
| FR-509 | System shall store token usage and cost for each AI call | Must Have |

---

### Module 6: Match Scoring (FR-6xx)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-601 | System shall compute overall match score (0–100) for each job-resume pair | Must Have |
| FR-602 | System shall compute Skill Match Score (weight: 50%) | Must Have |
| FR-603 | System shall compute Experience Match Score (weight: 25%) | Must Have |
| FR-604 | System shall compute Location Match Score (weight: 15%) | Must Have |
| FR-605 | System shall compute Domain Match Score (weight: 10%) | Must Have |
| FR-606 | System shall store all component scores in the database | Must Have |
| FR-607 | System shall filter and surface only jobs above a configurable threshold (default: 70%) | Must Have |
| FR-608 | System shall re-score if resume is updated | Should Have |

---

### Module 7: Resume Tailoring & Document Generation (FR-7xx)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-701 | System shall tailor resume bullet points for a specific JD using LLM | Must Have |
| FR-702 | System shall reorder skills section to prioritise JD-required skills | Must Have |
| FR-703 | System shall add missing ATS keywords naturally to resume text | Must Have |
| FR-704 | System shall generate a tailored cover letter | Must Have |
| FR-705 | System shall generate a PDF of the tailored resume using OpenPDF | Must Have |
| FR-706 | System shall generate a PDF of the cover letter | Must Have |
| FR-707 | System shall log all modifications made to the original resume | Must Have |
| FR-708 | System shall compute ATS simulation score for the tailored resume | Should Have |
| FR-709 | System shall allow users to download generated PDFs | Must Have |

---

### Module 8: Notifications (FR-8xx)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-801 | System shall send a daily digest email with top job matches | Must Have |
| FR-802 | System shall send real-time email alert when match score exceeds user threshold | Must Have |
| FR-803 | System shall send application status update notifications | Must Have |
| FR-804 | System shall send weekly summary report | Should Have |
| FR-805 | System shall allow users to configure notification preferences | Must Have |
| FR-806 | System shall log all sent notifications with delivery status | Must Have |
| FR-807 | System shall support email unsubscribe | Must Have |

---

### Module 9: Application Tracking Dashboard (FR-9xx)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-901 | System shall display all applications with status: PENDING, APPLIED, INTERVIEW, OFFER, REJECTED | Must Have |
| FR-902 | System shall allow manual status updates by user | Must Have |
| FR-903 | System shall display match score and component breakdown per application | Must Have |
| FR-904 | System shall provide filtering and sorting on the dashboard | Should Have |
| FR-905 | System shall display analytics: applications per week, interview conversion rate, top matched skills | Should Have |

---

## 3. Non-Functional Requirements

| ID | Category | Requirement | Target |
|----|----------|-------------|--------|
| NFR-01 | **Performance** | API response time (p95) for read operations | ≤ 500 ms |
| NFR-02 | **Performance** | API response time (p95) for write operations | ≤ 2,000 ms |
| NFR-03 | **Performance** | AI pipeline end-to-end processing time per job | ≤ 10 minutes |
| NFR-04 | **Performance** | PDF generation time | ≤ 5 seconds |
| NFR-05 | **Scalability** | Concurrent users without degradation | ≥ 100 |
| NFR-06 | **Scalability** | Jobs processed per day | ≥ 1,000 |
| NFR-07 | **Availability** | System uptime (business hours) | ≥ 99.5% |
| NFR-08 | **Availability** | Scheduled scraper success rate | ≥ 98% |
| NFR-09 | **Security** | All data in transit encrypted | TLS 1.3 |
| NFR-10 | **Security** | All PII encrypted at rest | AES-256 |
| NFR-11 | **Security** | JWT tokens expire and rotate | 24h access / 7d refresh |
| NFR-12 | **Usability** | User onboarding time (new user to first match) | ≤ 5 minutes |
| NFR-13 | **Reliability** | Failed AI calls automatically retried | ≥ 3 attempts with backoff |
| NFR-14 | **AI Safety** | All tailored resumes reviewed by user before submission | Enforced by workflow |
| NFR-15 | **Compliance** | GDPR — right to erasure implemented | Full data deletion on request |

---

*Document End — SRS v1.0*
