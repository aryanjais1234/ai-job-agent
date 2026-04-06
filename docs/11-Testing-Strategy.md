# Testing Strategy
## AI Job Agent — Automated Job Application System
**Version:** 1.0  
**Date:** 2024-01-01

---

## Test Pyramid

```
                    ┌─────────────┐
                    │  E2E Tests  │   5%  (Playwright / Cypress)
                    │  (3 tests)  │
                  ┌─┴─────────────┴─┐
                  │ Integration Tests│  25%  (Spring Boot Test + Testcontainers)
                  │   (6 tests)      │
                ┌─┴──────────────────┴─┐
                │     Unit Tests        │  70%  (JUnit 5 + Mockito)
                │     (9 tests)         │
                └───────────────────────┘
```

**Coverage Target:** ≥ 80% line coverage for service layer  
**Test Naming Convention:** `methodName_scenario_expectedResult`

---

## Test Environment

| Environment | Database | Cache | MQ | AI |
|-------------|----------|-------|----|----|
| Unit | H2 / Mock | MockBean | MockBean | Mockito stub |
| Integration | MySQL (Testcontainers) | Redis (Testcontainers) | RabbitMQ (Testcontainers) | Wiremock |
| E2E | Docker Compose (full stack) | Real | Real | Wiremock |

---

## Test Cases

### Unit Tests

#### TC-01 — MatchScoreService: Perfect Match

| Field | Value |
|-------|-------|
| **Test ID** | TC-01 |
| **Component** | `MatchScoreService` |
| **Method** | `computeScore()` |
| **Scenario** | All required skills present, experience in range, same location, matching domain |
| **Input** | Resume: Java, Spring Boot, MySQL; Job requires Java, Spring Boot, MySQL; 3 yrs exp (range 2-5); Bangalore |
| **Expected** | `overallScore = 100.0`, `skillScore = 100.0`, `experienceScore = 100.0`, `locationScore = 100.0` |
| **Type** | Unit (JUnit 5 + Mockito) |

---

#### TC-02 — MatchScoreService: Missing Skills

| Field | Value |
|-------|-------|
| **Test ID** | TC-02 |
| **Component** | `MatchScoreService` |
| **Method** | `computeScore()` |
| **Scenario** | User has 2 of 4 required skills |
| **Input** | Resume: Java, MySQL; Job requires Java, Spring Boot, Kafka, MySQL |
| **Expected** | `skillScore = 50.0`, `overallScore < 80` |
| **Type** | Unit |

---

#### TC-03 — MatchScoreService: Remote Job Any Location

| Field | Value |
|-------|-------|
| **Test ID** | TC-03 |
| **Component** | `MatchScoreService` |
| **Method** | `computeScore()` |
| **Scenario** | Job is fully remote; user location different from job location |
| **Input** | Job: remote_type=REMOTE, location=New York; User: location=Bangalore |
| **Expected** | `locationScore = 100.0` |
| **Type** | Unit |

---

#### TC-04 — MatchScoreService: Underexperienced

| Field | Value |
|-------|-------|
| **Test ID** | TC-04 |
| **Component** | `MatchScoreService` |
| **Method** | `computeScore()` |
| **Scenario** | User has 1 yr experience; job requires 3-6 years |
| **Input** | `userYears=1`, `expMin=3`, `expMax=6` |
| **Expected** | `experienceScore = 60.0` (gap=2, penalty=40) |
| **Type** | Unit |

---

#### TC-05 — JwtService: Token Generation and Validation

| Field | Value |
|-------|-------|
| **Test ID** | TC-05 |
| **Component** | `JwtService` |
| **Method** | `generateAccessToken()`, `validateToken()` |
| **Scenario** | Generate token for user; validate it returns correct userId |
| **Input** | User with id=1, email=test@example.com |
| **Expected** | Token valid; claims contain userId=1, email=test@example.com |
| **Type** | Unit |

---

#### TC-06 — JwtService: Expired Token Rejected

| Field | Value |
|-------|-------|
| **Test ID** | TC-06 |
| **Component** | `JwtService` |
| **Method** | `validateToken()` |
| **Scenario** | Token with expiry in the past |
| **Input** | Token with `exp = System.currentTimeMillis() - 1000` |
| **Expected** | Throws `JwtExpiredException` |
| **Type** | Unit |

---

#### TC-07 — ResumeParserService: Skill Extraction

| Field | Value |
|-------|-------|
| **Test ID** | TC-07 |
| **Component** | `ResumeParserService` |
| **Method** | `extractSkills()` |
| **Scenario** | PDF resume with known skill list |
| **Input** | PDF file containing "Proficient in Java, Spring Boot, and Docker" |
| **Expected** | Skills list contains: ["Java", "Spring Boot", "Docker"] |
| **Type** | Unit (with test PDF fixture) |

---

#### TC-08 — AuthService: Duplicate Email Rejected

| Field | Value |
|-------|-------|
| **Test ID** | TC-08 |
| **Component** | `AuthService` |
| **Method** | `register()` |
| **Scenario** | Register with email that already exists |
| **Input** | `{email: "existing@test.com", ...}` |
| **Expected** | Throws `DuplicateResourceException` |
| **Type** | Unit |

---

#### TC-09 — AuthService: Invalid Password Format

| Field | Value |
|-------|-------|
| **Test ID** | TC-09 |
| **Component** | `AuthController` validation |
| **Method** | `POST /auth/register` |
| **Scenario** | Password too short (< 8 chars) |
| **Input** | `{password: "abc"}` |
| **Expected** | HTTP 400, code=VALIDATION_FAILED |
| **Type** | Unit (MockMvc) |

---

### Integration Tests

#### TC-10 — Resume Upload End-to-End

| Field | Value |
|-------|-------|
| **Test ID** | TC-10 |
| **Component** | `ResumeController` → `ResumeService` → MySQL |
| **Scenario** | User uploads PDF; system parses and stores |
| **Input** | Valid JWT + PDF file (test fixture: `src/test/resources/test-resume.pdf`) |
| **Expected** | HTTP 201; `resumes` table has new row; `resume_skills` has ≥ 3 skills |
| **Type** | Integration (Testcontainers MySQL) |

---

#### TC-11 — Job Scrape → RabbitMQ → Persist

| Field | Value |
|-------|-------|
| **Test ID** | TC-11 |
| **Component** | `JobRawConsumer` → `JobPersistenceService` → MySQL |
| **Scenario** | Message published to `jobs.raw`; consumer persists to DB |
| **Input** | Sample job JSON on `jobs.raw` queue |
| **Expected** | Row in `jobs` table with correct title, company, source_url |
| **Type** | Integration (Testcontainers MySQL + RabbitMQ) |

---

#### TC-12 — Duplicate Job Rejected

| Field | Value |
|-------|-------|
| **Test ID** | TC-12 |
| **Component** | `JobPersistenceService` |
| **Scenario** | Same job published twice; only one row created |
| **Input** | Two identical job messages with same `source_url` |
| **Expected** | Only 1 row in `jobs` table; second publish is no-op |
| **Type** | Integration |

---

#### TC-13 — JWT Auth Filter Blocks Unauthenticated Request

| Field | Value |
|-------|-------|
| **Test ID** | TC-13 |
| **Component** | `JwtAuthFilter` |
| **Scenario** | Request to protected endpoint without JWT |
| **Input** | `GET /api/v1/jobs/matches` (no Authorization header) |
| **Expected** | HTTP 401, code=AUTHENTICATION_REQUIRED |
| **Type** | Integration (Spring MockMvc) |

---

#### TC-14 — Daily Digest Email Sent

| Field | Value |
|-------|-------|
| **Test ID** | TC-14 |
| **Component** | `DailyDigestService` + `EmailNotificationService` |
| **Scenario** | User has 5 matches ≥ threshold; digest triggered |
| **Input** | User with 5 job_matches in DB, email_enabled=true |
| **Expected** | GreenMail (or mock SMTP) receives 1 email with correct subject; notification_logs has 1 row |
| **Type** | Integration (GreenMail) |

---

#### TC-15 — PDF Generation Produces Valid File

| Field | Value |
|-------|-------|
| **Test ID** | TC-15 |
| **Component** | `PdfGeneratorService` |
| **Scenario** | Tailored resume content → PDF file |
| **Input** | Sample tailored_content string |
| **Expected** | File exists at expected path; file size > 10 KB; file starts with `%PDF` |
| **Type** | Integration |

---

### End-to-End Tests

#### TC-16 — Full Registration + Onboarding Flow

| Field | Value |
|-------|-------|
| **Test ID** | TC-16 |
| **Component** | Full stack |
| **Scenario** | New user registers, verifies email, uploads resume, sets preferences |
| **Steps** | 1. POST /auth/register → 2. GET /auth/verify?token=... → 3. POST /resumes → 4. PUT /users/preferences |
| **Expected** | All steps return success; user can subsequently GET /jobs/matches |
| **Type** | E2E (REST Assured or Playwright) |

---

#### TC-17 — Match Score Triggers Tailoring Pipeline

| Field | Value |
|-------|-------|
| **Test ID** | TC-17 |
| **Component** | Full pipeline |
| **Scenario** | High-score match (≥ 75) triggers automatic tailoring |
| **Steps** | 1. Publish test job to jobs.raw → 2. Wait for pipeline → 3. GET /tailor/{id} |
| **Expected** | `tailored_resumes` table has entry for user; `pdfReady = true` |
| **Type** | E2E (full Docker Compose + Wiremock for OpenAI) |

---

#### TC-18 — Application Status Update Flow

| Field | Value |
|-------|-------|
| **Test ID** | TC-18 |
| **Component** | `ApplicationController` |
| **Scenario** | User updates application status from APPLIED to INTERVIEW |
| **Steps** | 1. POST /applications → 2. PUT /applications/{id} with status=INTERVIEW, interviewDate |
| **Expected** | HTTP 200; DB status=INTERVIEW; notification_logs has APPLICATION_UPDATE entry |
| **Type** | E2E |

---

## Test Coverage Targets

| Layer | File Coverage | Line Coverage |
|-------|--------------|---------------|
| Service Layer | ≥ 90% | ≥ 80% |
| Controller Layer | ≥ 85% | ≥ 75% |
| Repository Layer | ≥ 70% | ≥ 65% |
| Utility Classes | ≥ 95% | ≥ 90% |
| AI Prompt Templates | Manual review | — |

---

## Test Tooling

| Tool | Purpose |
|------|---------|
| JUnit 5 | Unit and integration test runner |
| Mockito | Mocking dependencies in unit tests |
| Testcontainers | Spin up MySQL, Redis, RabbitMQ for integration tests |
| Spring Boot Test | `@SpringBootTest` for full context integration tests |
| MockMvc | Controller layer testing without server |
| GreenMail | Embedded SMTP server for email tests |
| Wiremock | Mock OpenAI API responses |
| JaCoCo | Code coverage reporting |

---

*Document End — Testing Strategy v1.0*
