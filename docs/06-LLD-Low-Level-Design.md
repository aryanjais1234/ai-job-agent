# Low-Level Design (LLD)
## AI Job Agent — Automated Job Application System
**Version:** 1.0  
**Date:** 2024-01-01  
**Author:** Aryan Jaiswal

---

## Table of Contents
1. [Package Structure](#package-structure)
2. [API Specification](#api-specification)
3. [Class Diagrams](#class-diagrams)
4. [Sequence Diagrams](#sequence-diagrams)
5. [Match Score Algorithm](#match-score-algorithm)
6. [Security Design](#security-design)
7. [Error Handling Standard](#error-handling-standard)

---

## 1. Package Structure

```
backend/src/main/java/com/aryanjais/aijobagent/
│
├── AiJobAgentApplication.java          # @SpringBootApplication entry point
│
├── config/
│   ├── SecurityConfig.java             # Spring Security filter chain, CORS
│   ├── JwtConfig.java                  # JWT secret, expiry settings
│   ├── RabbitMQConfig.java             # Exchange, queue, binding declarations
│   ├── RedisConfig.java                # RedisTemplate configuration
│   ├── OpenApiConfig.java              # Springdoc Swagger configuration
│   └── SchedulerConfig.java            # @EnableScheduling configuration
│
├── controller/
│   ├── AuthController.java             # POST /auth/register, /login, /refresh, /verify
│   ├── UserController.java             # GET/PUT /users/profile, /users/preferences
│   ├── ResumeController.java           # POST/GET /resumes, DELETE /resumes/{id}
│   ├── JobController.java              # GET /jobs, /jobs/{id}, /jobs/matches
│   ├── TailoringController.java        # POST /tailor/{jobId}, GET /tailor/{id}
│   ├── DocumentController.java         # GET /documents/resume/{id}, /cover-letter/{id}
│   ├── ApplicationController.java      # GET/POST/PUT /applications
│   ├── NotificationController.java     # GET/PUT /notifications, /preferences
│   └── AdminController.java            # GET /admin/scrape-logs, /admin/ai-usage
│
├── service/
│   ├── auth/
│   │   ├── AuthService.java            # Registration, login, token refresh logic
│   │   ├── JwtService.java             # Token generation, validation, claims
│   │   └── EmailVerificationService.java # Verification token management
│   ├── resume/
│   │   ├── ResumeService.java          # Upload, parse, CRUD
│   │   ├── ResumeParserService.java    # Apache Tika / PDFBox text extraction
│   │   └── SkillExtractorService.java  # Regex + NLP skill extraction
│   ├── job/
│   │   ├── JobService.java             # CRUD, search, filtering
│   │   └── JobPersistenceService.java  # Deduplication, save from queue
│   ├── ai/
│   │   ├── OpenAiClient.java           # HTTP client wrapper, retry, circuit breaker
│   │   ├── JobAnalysisService.java     # JD → structured analysis via LLM
│   │   ├── ResumeTailoringService.java # Resume → tailored content via LLM
│   │   └── CoverLetterService.java     # JD + resume → cover letter via LLM
│   ├── matching/
│   │   └── MatchScoreService.java      # Weighted score computation
│   ├── document/
│   │   ├── PdfGeneratorService.java    # OpenPDF resume + cover letter rendering
│   │   └── FileStorageService.java     # File read/write from storage volume
│   ├── notification/
│   │   ├── EmailNotificationService.java # Spring Mail SMTP
│   │   ├── DailyDigestService.java     # Aggregate and send daily digest
│   │   └── NotificationLogService.java # Persist to notification_logs
│   └── tracking/
│       └── ApplicationTrackingService.java # Status transitions, CRUD
│
├── messaging/
│   ├── producer/
│   │   ├── JobRawProducer.java         # Publish to jobs.raw
│   │   ├── JobAnalyzeProducer.java     # Publish to jobs.analyze
│   │   ├── JobMatchProducer.java       # Publish to jobs.match
│   │   ├── JobTailorProducer.java      # Publish to jobs.tailor
│   │   ├── DocGenerateProducer.java    # Publish to docs.generate
│   │   └── NotifySendProducer.java     # Publish to notify.send
│   └── consumer/
│       ├── JobRawConsumer.java         # Consume jobs.raw → persist
│       ├── JobAnalyzeConsumer.java     # Consume jobs.analyze → AI analysis
│       ├── JobMatchConsumer.java       # Consume jobs.match → score
│       ├── JobTailorConsumer.java      # Consume jobs.tailor → tailor
│       ├── DocGenerateConsumer.java    # Consume docs.generate → PDF
│       └── NotifySendConsumer.java     # Consume notify.send → email
│
├── repository/
│   ├── UserRepository.java
│   ├── UserPreferenceRepository.java
│   ├── ResumeRepository.java
│   ├── ResumeSkillRepository.java
│   ├── ResumeExperienceRepository.java
│   ├── ResumeEducationRepository.java
│   ├── JobRepository.java
│   ├── JobAnalysisRepository.java
│   ├── JobSkillRepository.java
│   ├── JobMatchRepository.java
│   ├── TailoredResumeRepository.java
│   ├── CoverLetterRepository.java
│   ├── ApplicationRepository.java
│   ├── NotificationPreferenceRepository.java
│   ├── NotificationLogRepository.java
│   ├── ScrapeLogRepository.java
│   └── AiUsageLogRepository.java
│
├── entity/
│   ├── User.java
│   ├── UserPreference.java
│   ├── Resume.java
│   ├── ResumeSkill.java
│   ├── ResumeExperience.java
│   ├── ResumeEducation.java
│   ├── Job.java
│   ├── JobAnalysis.java
│   ├── JobSkill.java
│   ├── JobMatch.java
│   ├── TailoredResume.java
│   ├── CoverLetter.java
│   ├── Application.java
│   ├── NotificationPreference.java
│   ├── NotificationLog.java
│   ├── ScrapeLog.java
│   └── AiUsageLog.java
│
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── UpdateProfileRequest.java
│   │   ├── UpdatePreferencesRequest.java
│   │   └── UpdateApplicationStatusRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── UserProfileResponse.java
│       ├── ResumeResponse.java
│       ├── JobResponse.java
│       ├── JobMatchResponse.java
│       ├── TailoredResumeResponse.java
│       └── ApplicationResponse.java
│
├── security/
│   ├── JwtAuthFilter.java              # OncePerRequestFilter JWT validation
│   ├── CustomUserDetailsService.java   # UserDetailsService implementation
│   └── SecurityConstants.java          # PUBLIC_ENDPOINTS whitelist
│
├── scheduler/
│   ├── ScrapeScheduler.java            # @Scheduled daily scrape trigger
│   └── DigestScheduler.java            # @Scheduled daily digest email
│
├── exception/
│   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── AuthenticationException.java
│   ├── FileProcessingException.java
│   └── AiServiceException.java
│
└── util/
    ├── SkillNormalizer.java            # Normalise skill name variants
    ├── TextSanitizer.java              # Strip HTML, normalise whitespace
    └── ScoreFormatter.java             # Round and format match scores
```

---

## 2. API Specification

### Authentication APIs

| Method | Endpoint | Auth | Request | Response |
|--------|----------|------|---------|----------|
| POST | `/api/v1/auth/register` | None | `RegisterRequest` | `201 AuthResponse` |
| POST | `/api/v1/auth/login` | None | `LoginRequest` | `200 AuthResponse` |
| POST | `/api/v1/auth/refresh` | Refresh JWT | — | `200 AuthResponse` |
| GET | `/api/v1/auth/verify?token=` | None | — | `200 OK` |
| POST | `/api/v1/auth/forgot-password` | None | `{email}` | `200 OK` |
| POST | `/api/v1/auth/reset-password` | None | `{token, newPassword}` | `200 OK` |

### User APIs

| Method | Endpoint | Auth | Request | Response |
|--------|----------|------|---------|----------|
| GET | `/api/v1/users/profile` | JWT | — | `200 UserProfileResponse` |
| PUT | `/api/v1/users/profile` | JWT | `UpdateProfileRequest` | `200 UserProfileResponse` |
| GET | `/api/v1/users/preferences` | JWT | — | `200 PreferenceResponse` |
| PUT | `/api/v1/users/preferences` | JWT | `UpdatePreferencesRequest` | `200 PreferenceResponse` |
| DELETE | `/api/v1/users/account` | JWT | — | `204 No Content` |

### Resume APIs

| Method | Endpoint | Auth | Request | Response |
|--------|----------|------|---------|----------|
| POST | `/api/v1/resumes` | JWT | Multipart file | `201 ResumeResponse` |
| GET | `/api/v1/resumes` | JWT | — | `200 List<ResumeResponse>` |
| GET | `/api/v1/resumes/{id}` | JWT | — | `200 ResumeResponse` |
| PUT | `/api/v1/resumes/{id}/primary` | JWT | — | `200 ResumeResponse` |
| DELETE | `/api/v1/resumes/{id}` | JWT | — | `204 No Content` |

### Job & Match APIs

| Method | Endpoint | Auth | Request | Response |
|--------|----------|------|---------|----------|
| GET | `/api/v1/jobs` | JWT | `?page&size&minScore` | `200 Page<JobMatchResponse>` |
| GET | `/api/v1/jobs/{id}` | JWT | — | `200 JobResponse` |
| GET | `/api/v1/jobs/matches` | JWT | `?minScore=70` | `200 List<JobMatchResponse>` |

### Tailoring & Document APIs

| Method | Endpoint | Auth | Request | Response |
|--------|----------|------|---------|----------|
| POST | `/api/v1/tailor/{jobId}` | JWT | — | `202 Accepted` |
| GET | `/api/v1/tailor/{id}` | JWT | — | `200 TailoredResumeResponse` |
| GET | `/api/v1/documents/resume/{tailoredId}` | JWT | — | `200 application/pdf` |
| GET | `/api/v1/documents/cover-letter/{coverId}` | JWT | — | `200 application/pdf` |

### Application Tracking APIs

| Method | Endpoint | Auth | Request | Response |
|--------|----------|------|---------|----------|
| GET | `/api/v1/applications` | JWT | `?status&page` | `200 Page<ApplicationResponse>` |
| POST | `/api/v1/applications` | JWT | `{jobId, notes}` | `201 ApplicationResponse` |
| PUT | `/api/v1/applications/{id}` | JWT | `UpdateApplicationStatusRequest` | `200 ApplicationResponse` |
| DELETE | `/api/v1/applications/{id}` | JWT | — | `204 No Content` |

---

## 3. Class Diagrams

### Core Domain Classes

```
┌─────────────────────────────────┐
│            User                 │
├─────────────────────────────────┤
│ - id: Long                      │
│ - email: String                 │
│ - passwordHash: String          │
│ - fullName: String              │
│ - phone: String                 │
│ - linkedinUrl: String           │
│ - experienceYears: Integer      │
│ - isActive: Boolean             │
│ - emailVerified: Boolean        │
│ - createdAt: LocalDateTime      │
├─────────────────────────────────┤
│ + getResumes(): List<Resume>    │
│ + getPreference(): UserPref     │
│ + getMatches(): List<JobMatch>  │
└─────────────────────────────────┘
          │ 1
          │
          │ *
┌─────────────────────────────────┐
│            Resume               │
├─────────────────────────────────┤
│ - id: Long                      │
│ - userId: Long                  │
│ - originalFilename: String      │
│ - filePath: String              │
│ - parsedText: String            │
│ - skillsJson: String            │
│ - isPrimary: Boolean            │
├─────────────────────────────────┤
│ + getSkills(): List<ResumeSkill>│
│ + getExperiences(): List<...>   │
└─────────────────────────────────┘
          │ 1
          │
          │ *
┌─────────────────────────────────┐
│          JobMatch               │
├─────────────────────────────────┤
│ - id: Long                      │
│ - userId: Long                  │
│ - resumeId: Long                │
│ - jobId: Long                   │
│ - overallScore: BigDecimal      │
│ - skillScore: BigDecimal        │
│ - experienceScore: BigDecimal   │
│ - locationScore: BigDecimal     │
│ - domainScore: BigDecimal       │
│ - status: MatchStatus           │
└─────────────────────────────────┘
```

### Service Layer

```
┌──────────────────────┐     ┌──────────────────────┐
│  MatchScoreService   │     │  OpenAiClient        │
├──────────────────────┤     ├──────────────────────┤
│ - jobAnalysisRepo    │     │ - apiKey: String     │
│ - resumeSkillRepo    │     │ - httpClient         │
│ - jobMatchRepo       │     │ - retryTemplate      │
├──────────────────────┤     ├──────────────────────┤
│ + computeScore()     │     │ + chat(prompt): String│
│ + skillScore()       │     │ + withRetry()        │
│ + experienceScore()  │     └──────────────────────┘
│ + locationScore()    │            ▲
│ + domainScore()      │            │ uses
└──────────────────────┘     ┌──────────────────────┐
                             │ JobAnalysisService   │
                             ├──────────────────────┤
                             │ + analyzeJd(Job)     │
                             │ + parseResponse()    │
                             └──────────────────────┘
```

---

## 4. Sequence Diagrams

### 4.1 Resume Tailoring Flow

```
User        Frontend       Backend         RabbitMQ        OpenAI
 │               │              │               │               │
 │ Click Tailor  │              │               │               │
 │──────────────▶│              │               │               │
 │               │ POST /tailor │               │               │
 │               │─────────────▶│               │               │
 │               │              │ Validate JWT  │               │
 │               │              │ Load Resume   │               │
 │               │              │ Load JobMatch │               │
 │               │  202 Accepted│               │               │
 │               │◀─────────────│               │               │
 │               │              │               │               │
 │               │              │ Publish to    │               │
 │               │              │ jobs.tailor   │               │
 │               │              │──────────────▶│               │
 │               │              │               │               │
 │               │              │  (async)      │ Consume queue │
 │               │              │               │ Build prompt  │
 │               │              │               │──────────────▶│
 │               │              │               │ Tailored text │
 │               │              │               │◀──────────────│
 │               │              │               │ Save to DB    │
 │               │              │               │ Publish to    │
 │               │              │               │ docs.generate │
 │               │              │               │               │
 │ Poll GET /tailor/{id}        │               │               │
 │──────────────▶│              │               │               │
 │               │──────────────▶│              │               │
 │               │ 200 TailoredResumeResponse   │               │
 │               │◀─────────────│               │               │
 │◀──────────────│              │               │               │
```

### 4.2 Daily Pipeline Flow

```
Cron Scheduler      FastAPI Scraper      RabbitMQ         Spring Boot
       │                    │                │                  │
 6 AM  │                    │                │                  │
 Tick  │                    │                │                  │
       │ HTTP POST          │                │                  │
       │ /scrape/all        │                │                  │
       │───────────────────▶│                │                  │
       │                    │ Playwright     │                  │
       │                    │ scrape jobs    │                  │
       │                    │ (Indeed/LI/NK) │                  │
       │                    │                │                  │
       │                    │ Publish N jobs │                  │
       │                    │ to jobs.raw    │                  │
       │                    │───────────────▶│                  │
       │                    │                │ Consume jobs.raw │
       │                    │                │─────────────────▶│
       │                    │                │                  │ Dedup
       │                    │                │                  │ Save
       │                    │                │                  │ Publish
       │                    │                │                  │ jobs.analyze
       │                    │                │◀─────────────────│
       │                    │                │ Consume & Analyze│
       │                    │                │─────────────────▶│
       │                    │                │                  │ Call OpenAI
       │                    │                │                  │ Save analysis
       │                    │                │                  │ Publish jobs.match
       │                    │                │◀─────────────────│
       │                    │                │ Score all users  │
       │                    │                │─────────────────▶│
       │                    │                │                  │ Save job_matches
       │                    │                │                  │ Publish jobs.tailor
       │                    │                │                  │ (for score ≥ 70)
```

---

## 5. Match Score Algorithm

### Formula

```
overallScore = (skillScore × 0.50) + (experienceScore × 0.25) 
             + (locationScore × 0.15) + (domainScore × 0.10)
```

### Skill Score (Weight: 50%)

```
skillScore = (matchedRequiredSkills / totalRequiredSkills) × 100
```

- Build `resumeSkillSet` = normalised set of all skills from resume
- Build `requiredSkillSet` = set of required skills from `job_skills` where `is_required = true`
- `matchedRequiredSkills` = `requiredSkillSet ∩ resumeSkillSet` (case-insensitive, normalised)
- Bonus: each nice-to-have skill match adds `2` points (capped at 10 bonus points)

### Experience Score (Weight: 25%)

```
userYears = user.experienceYears

IF userYears >= job.experienceMin AND userYears <= job.experienceMax + 2:
    experienceScore = 100
ELSE IF userYears < job.experienceMin:
    gap = job.experienceMin - userYears
    experienceScore = MAX(0, 100 - (gap × 20))
ELSE:  # overqualified
    experienceScore = 80  # slight penalty, still good match
```

### Location Score (Weight: 15%)

```
IF job.remoteType == "REMOTE":
    locationScore = 100
ELSE IF job.location == user.location (city match):
    locationScore = 100
ELSE IF job.location == user.location (state/country match):
    locationScore = 60
ELSE IF user.preference.remoteOk AND job.remoteType in ["HYBRID", "REMOTE"]:
    locationScore = 80
ELSE:
    locationScore = 0
```

### Domain Score (Weight: 10%)

```
IF job.domain in user.preference.industries:
    domainScore = 100
ELSE IF job.domain SIMILAR_TO user.resumeDomain:
    domainScore = 70
ELSE:
    domainScore = 30
```

---

## 6. Security Design

### JWT Token Strategy

```
Access Token:
  - Algorithm: HS256
  - Expiry: 24 hours
  - Claims: userId, email, roles, iat, exp
  - Storage: HttpOnly Cookie (browser) or Authorization header (API)

Refresh Token:
  - Algorithm: HS256
  - Expiry: 7 days
  - Storage: HttpOnly Cookie only
  - Revocation: Redis blocklist (token JTI)
```

### Security Layers

| Layer | Mechanism |
|-------|-----------|
| Transport | TLS 1.3 enforced by Nginx |
| Authentication | JWT Bearer token validation in `JwtAuthFilter` |
| Authorisation | `@PreAuthorize` annotations; users can only access their own data |
| Password Storage | BCrypt with strength 12 |
| PII Encryption | AES-256-GCM for sensitive fields at rest |
| Rate Limiting | 100 req/min per IP; 20 AI calls/hr per user (Redis counters) |
| SQL Injection | JPA parameterised queries only; no native SQL with user input |
| XSS | Response Content-Type: application/json; CSP headers via Nginx |
| CSRF | Disabled for stateless JWT API; SameSite=Strict cookie flag |

---

## 7. Error Handling Standard

### Error Response Format

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "RESOURCE_NOT_FOUND",
  "message": "Resume with id 42 not found",
  "path": "/api/v1/resumes/42",
  "traceId": "abc-123-def-456"
}
```

### HTTP Status Code Mapping

| Scenario | HTTP Status | Error Code |
|----------|------------|-----------|
| Successful creation | 201 Created | — |
| Successful deletion | 204 No Content | — |
| Resource not found | 404 Not Found | `RESOURCE_NOT_FOUND` |
| Duplicate resource | 409 Conflict | `DUPLICATE_RESOURCE` |
| Validation failure | 400 Bad Request | `VALIDATION_FAILED` |
| Unauthenticated | 401 Unauthorized | `AUTHENTICATION_REQUIRED` |
| Insufficient permissions | 403 Forbidden | `ACCESS_DENIED` |
| File too large | 413 Payload Too Large | `FILE_TOO_LARGE` |
| Unsupported file type | 415 Unsupported Media Type | `UNSUPPORTED_FILE_TYPE` |
| Rate limit exceeded | 429 Too Many Requests | `RATE_LIMIT_EXCEEDED` |
| AI service error | 503 Service Unavailable | `AI_SERVICE_UNAVAILABLE` |
| Internal server error | 500 Internal Server Error | `INTERNAL_ERROR` |

---

*Document End — LLD v1.0*
