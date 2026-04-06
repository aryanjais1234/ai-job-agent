# Product Requirements Document (PRD)
## AI Job Agent — Automated Job Application System
**Version:** 1.0  
**Date:** 2024-01-01  
**Author:** Aryan Jaiswal  
**Status:** Approved

---

## Table of Contents
1. [MoSCoW Feature Priority](#moscow-feature-priority)
2. [User Stories](#user-stories)
3. [UI Wireframes](#ui-wireframes)

---

## 1. MoSCoW Feature Priority

### Must Have (Launch Blockers)

| Feature | Rationale |
|---------|-----------|
| User registration & JWT auth | Gate for all personalised features |
| Resume upload & parsing | Core data input for match/tailoring |
| Job scraping (Indeed, LinkedIn, Naukri) | Primary value: automated discovery |
| LLM-based JD analysis | Enables intelligent matching |
| Weighted match scoring | Filters relevant jobs from noise |
| AI resume tailoring | Core differentiator vs. manual process |
| PDF generation (resume + cover letter) | Deliverable users take to employers |
| Daily digest email notifications | Closes the loop without requiring login |
| Application tracking dashboard | Transparency and trust |

### Should Have (MVP+)

| Feature | Rationale |
|---------|-----------|
| Weekly summary report email | Engagement and retention |
| ATS simulation score display | Reassures user of quality |
| Resume modification log | Transparency and trust |
| Multiple resume versions | Power users with varied target roles |
| Company blacklist | Reduces irrelevant noise |
| Analytics charts on dashboard | Motivation and self-improvement |

### Could Have (v1.1)

| Feature | Rationale |
|---------|-----------|
| LinkedIn OAuth login | Reduced friction at registration |
| Skill gap analysis | Career development angle |
| Browser extension for manual saves | Covers niche sources |
| Bulk application review | Efficiency for high-volume users |
| Dark mode | Accessibility and preference |

### Won't Have (v1.0)

| Feature | Reason |
|---------|--------|
| Automated form submission | ToS violations; legal risk |
| Mobile native app | Responsive web is sufficient for MVP |
| Interview preparation AI | Out of scope; separate product |
| Payment processing | Self-hosted MVP; monetisation is v2 |

---

## 2. User Stories

### US-01 — User Registration

**As an** active job seeker,  
**I want to** create an account with my email and password,  
**So that** I can access personalised job matching.

**Acceptance Criteria:**
- [ ] Registration form accepts: full name, email, password (min 8 chars, 1 uppercase, 1 number)
- [ ] Verification email sent within 60 seconds of registration
- [ ] Unverified accounts cannot access protected routes
- [ ] Duplicate email returns HTTP 409 with descriptive error

---

### US-02 — Resume Upload

**As an** active job seeker,  
**I want to** upload my resume in PDF or DOCX format,  
**So that** the system can match me against relevant jobs.

**Acceptance Criteria:**
- [ ] Accepts PDF and DOCX; rejects other formats with HTTP 415
- [ ] Files > 10 MB rejected with HTTP 413
- [ ] Parsed data (skills, experience, education) displayed for review within 30 seconds
- [ ] User can confirm or manually edit parsed data

---

### US-03 — Job Preference Setup

**As an** active job seeker,  
**I want to** configure my job preferences (titles, locations, salary, remote),  
**So that** I only receive relevant job matches.

**Acceptance Criteria:**
- [ ] Supports multiple job titles and locations
- [ ] Salary range inputs with currency selector
- [ ] Remote preference: Remote / Hybrid / On-site / Any
- [ ] Preferences saved and immediately applied to next scrape cycle

---

### US-04 — Automated Job Scraping

**As a** job seeker,  
**I want** jobs automatically scraped from Indeed, LinkedIn, and Naukri daily,  
**So that** I don't need to visit each platform manually.

**Acceptance Criteria:**
- [ ] Scrape runs at 6 AM IST daily without manual trigger
- [ ] Duplicate jobs not stored (deduplicated by source URL)
- [ ] Scrape completion logged with count of new/updated jobs
- [ ] User notified if no new jobs found matching preferences

---

### US-05 — Match Score Review

**As a** job seeker,  
**I want to** see a match score for each job with score breakdown,  
**So that** I can prioritise applications strategically.

**Acceptance Criteria:**
- [ ] Overall score (0–100) prominently displayed
- [ ] Breakdown visible: Skill %, Experience %, Location %, Domain %
- [ ] Jobs sorted by match score by default (descending)
- [ ] Low-score jobs (< 70) hidden by default but accessible via filter

---

### US-06 — AI Resume Tailoring

**As a** job seeker,  
**I want** my resume automatically tailored for a specific job,  
**So that** my application passes ATS screening.

**Acceptance Criteria:**
- [ ] Tailoring initiated automatically for matches ≥ 70%
- [ ] Modifications log shows exactly what was changed and why
- [ ] Tailored resume reviewed by user before PDF generation
- [ ] ATS simulation score ≥ 85 guaranteed before delivery

---

### US-07 — Cover Letter Generation

**As a** job seeker,  
**I want** a personalised cover letter generated for each application,  
**So that** I stand out from generic applicants.

**Acceptance Criteria:**
- [ ] Cover letter references specific JD requirements and company name
- [ ] Tone matches role seniority (professional for senior, enthusiastic for junior)
- [ ] User can edit generated cover letter before PDF export
- [ ] Cover letter PDF exportable separately

---

### US-08 — Daily Digest Notification

**As a** passive job seeker,  
**I want** a daily email with my top job matches,  
**So that** I stay informed without actively checking the app.

**Acceptance Criteria:**
- [ ] Email delivered between 7–9 AM in user's timezone
- [ ] Shows top 5 matches with: title, company, location, score, link
- [ ] One-click unsubscribe link included
- [ ] Email renders correctly on Gmail, Outlook, Apple Mail

---

### US-09 — Application Tracking

**As a** job seeker,  
**I want to** track all my applications in one dashboard,  
**So that** I never miss a follow-up or deadline.

**Acceptance Criteria:**
- [ ] All applications listed with current status
- [ ] Status transitions: PENDING → APPLIED → INTERVIEW → OFFER / REJECTED
- [ ] User can manually update status and add notes
- [ ] Interview date field with calendar picker
- [ ] Dashboard filterable by status, date range, company

---

### US-10 — Data Privacy & Export

**As a** user concerned about privacy,  
**I want to** export all my data and request account deletion,  
**So that** I maintain control over my personal information.

**Acceptance Criteria:**
- [ ] Data export returns JSON with all user data within 24 hours
- [ ] Account deletion removes all PII within 72 hours
- [ ] Deletion confirmation email sent
- [ ] Deletion is irreversible with double-confirm prompt

---

## 3. UI Wireframes

### Screen 1 — Login / Registration

```
┌─────────────────────────────────────────────────────┐
│                  🧠 AI Job Agent                     │
│─────────────────────────────────────────────────────│
│                                                     │
│         [ Sign In ]    [ Create Account ]           │
│                                                     │
│  ┌───────────────────────────────────┐              │
│  │  Email Address                    │              │
│  │  ┌─────────────────────────────┐  │              │
│  │  │ priya@example.com           │  │              │
│  │  └─────────────────────────────┘  │              │
│  │                                   │              │
│  │  Password                         │              │
│  │  ┌─────────────────────────────┐  │              │
│  │  │ ••••••••••                  │  │              │
│  │  └─────────────────────────────┘  │              │
│  │                                   │              │
│  │  [        Sign In         ]       │              │
│  │                                   │              │
│  │  Forgot password? | Register       │              │
│  └───────────────────────────────────┘              │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

### Screen 2 — Onboarding (3-Step Wizard)

```
┌─────────────────────────────────────────────────────┐
│  Step 1 of 3: Upload Resume          ● ○ ○          │
│─────────────────────────────────────────────────────│
│                                                     │
│         ┌────────────────────────────┐              │
│         │                            │              │
│         │   📄 Drop your resume here  │              │
│         │   or click to browse       │              │
│         │                            │              │
│         │   Accepted: PDF, DOCX      │              │
│         │   Max size: 10 MB          │              │
│         └────────────────────────────┘              │
│                                                     │
│  ┌─── Parsed Skills ──────────────────┐             │
│  │  ✓ Java  ✓ Spring Boot  ✓ MySQL    │             │
│  │  ✓ React  ✓ Docker  ✓ Kubernetes   │             │
│  └───────────────────────────────────┘             │
│                                                     │
│                        [ Next: Preferences → ]      │
└─────────────────────────────────────────────────────┘
```

---

### Screen 3 — Dashboard (Main)

```
┌─────────────────────────────────────────────────────────────┐
│  🧠 AI Job Agent          Priya Sharma ▾    [🔔 3]          │
│─────────────────────────────────────────────────────────────│
│  ╔═══════════╗  ╔═══════════╗  ╔═══════════╗  ╔══════════╗  │
│  ║ Jobs      ║  ║ Applied   ║  ║ Interview ║  ║ ATS Avg  ║  │
│  ║ Matched   ║  ║           ║  ║           ║  ║          ║  │
│  ║    47     ║  ║    12     ║  ║     3     ║  ║   91%    ║  │
│  ╚═══════════╝  ╚═══════════╝  ╚═══════════╝  ╚══════════╝  │
│                                                             │
│  [ All ] [Pending] [Applied] [Interview]   🔍 Search  ▼ Sort│
│  ─────────────────────────────────────────────────────────  │
│  │ Senior Java Dev     Infosys      Bangalore   Score: 94 │  │
│  │ ████████████████████████░░░░░░░  [View] [Tailor] [↓PDF]│  │
│  ─────────────────────────────────────────────────────────  │
│  │ Backend Engineer    Razorpay     Remote      Score: 87 │  │
│  │ █████████████████░░░░░░░░░░░░░░  [View] [Tailor] [↓PDF]│  │
│  ─────────────────────────────────────────────────────────  │
│  │ Full Stack Dev      Swiggy       Mumbai      Score: 78 │  │
│  │ ███████████████░░░░░░░░░░░░░░░░  [View] [Tailor] [↓PDF]│  │
└─────────────────────────────────────────────────────────────┘
```

---

### Screen 4 — Resume Preview & Tailoring

```
┌─────────────────────────────────────────────────────────────┐
│  ← Back    Tailored Resume: Senior Java Dev @ Infosys        │
│─────────────────────────────────────────────────────────────│
│                                                             │
│  ATS Score: [████████████████████░░░░] 91/100  ✅ Good      │
│                                                             │
│  ┌─── Modifications Made (7) ──────────────────────────┐   │
│  │ ✏️  Added keyword "microservices" to Work Exp §2      │   │
│  │ ✏️  Reordered skills: Java, Spring Boot moved up      │   │
│  │ ✏️  Quantified bullet: "Led team" → "Led 5-eng team"  │   │
│  │ ✏️  Added "Kafka" to Skills (mentioned in JD)         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─── Resume Preview ──────────────────────────────────┐   │
│  │  PRIYA SHARMA                                        │   │
│  │  priya@example.com | +91 98765 43210                 │   │
│  │  Bangalore, India                                    │   │
│  │                                                      │   │
│  │  SKILLS                                              │   │
│  │  Java • Spring Boot • Microservices • Kafka ...      │   │
│  │                                                      │   │
│  │  EXPERIENCE                                          │   │
│  │  Software Engineer | TCS | 2021–Present              │   │
│  │  • Led 5-engineer team building microservices ...    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  [ Edit Resume ]    [ Download PDF ]    [ Apply & Track ]   │
└─────────────────────────────────────────────────────────────┘
```

---

### Screen 5 — Application Tracker

```
┌──────────────────────────────────────────────────────────────┐
│  📋 Application Tracker                      Export CSV ↓    │
│──────────────────────────────────────────────────────────────│
│  Filter: [ All ▼ ]  Date: [ Last 30 Days ▼ ]  🔍             │
│  ──────────────────────────────────────────────────────────  │
│  Company        Role               Status      Date    Score  │
│  ──────────────────────────────────────────────────────────  │
│  Infosys        Sr. Java Dev       INTERVIEW   Dec 15    94   │
│                                   📅 Interview: Dec 20        │
│  ──────────────────────────────────────────────────────────  │
│  Razorpay       Backend Eng        APPLIED     Dec 14    87   │
│                                   [Update Status ▼]          │
│  ──────────────────────────────────────────────────────────  │
│  Swiggy         Full Stack Dev     PENDING     Dec 13    78   │
│                                   [Tailor] [Mark Applied]    │
│  ──────────────────────────────────────────────────────────  │
│  Zepto          Java Engineer      REJECTED    Dec 10    82   │
│                                   📝 Reason: Over-qualified   │
│  ──────────────────────────────────────────────────────────  │
│                                                              │
│  Total: 12 applications | 3 Interviews | 25% conversion      │
└──────────────────────────────────────────────────────────────┘
```

---

*Document End — PRD v1.0*
