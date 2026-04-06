# Technical Specifications
## AI Job Agent — Automated Job Application System
**Version:** 1.0  
**Date:** 2024-01-01

---

## 1. AI Prompt Templates

### 1.1 JD Analysis Prompt

```
SYSTEM:
You are an expert technical recruiter and ATS specialist. Analyse the following job description 
and extract structured information. Respond ONLY with valid JSON matching the schema below.

USER:
Analyse this job description and return structured JSON:

Job Description:
---
{job_description}
---

Return JSON with this exact schema:
{
  "required_skills": ["string"],           // Must-have technical skills mentioned
  "nice_to_have_skills": ["string"],       // Optional/preferred skills
  "experience_min": number,               // Minimum years of experience (integer)
  "experience_max": number,               // Maximum years of experience (integer)
  "education_required": "string",         // e.g., "B.E./B.Tech in CS or equivalent"
  "domain": "string",                     // Industry domain e.g., "FinTech", "E-Commerce"
  "keywords": ["string"],                 // Top 10 ATS keywords for this role
  "seniority_level": "JUNIOR|MID|SENIOR|LEAD|MANAGER",
  "remote_type": "REMOTE|HYBRID|ON_SITE"
}

Rules:
- If a value cannot be determined, use null
- experience_min defaults to 0 if not specified
- Normalise skill names (e.g., "node.js" → "Node.js", "springboot" → "Spring Boot")
- Return ONLY the JSON object, no markdown, no explanation
```

---

### 1.2 Resume Tailoring Prompt

```
SYSTEM:
You are an expert resume writer and ATS optimisation specialist. Your task is to tailor a 
candidate's resume to maximise ATS score for a specific job description without fabricating 
experience or skills the candidate does not have.

USER:
Original Resume:
---
{original_resume_text}
---

Target Job Description:
---
{job_description}
---

Required Skills from JD: {required_skills_json}
Nice-to-have Skills from JD: {nice_to_have_skills_json}
ATS Keywords: {keywords_json}

Instructions:
1. Reorder the skills section to put JD-required skills first
2. Incorporate ATS keywords naturally into existing bullet points
3. Quantify bullet points where possible (add metrics if implied but not stated)
4. Strengthen action verbs (e.g., "worked on" → "engineered", "helped with" → "delivered")
5. Add any required skills the candidate has but did not mention explicitly
6. DO NOT add skills or experiences the candidate does not have
7. Maintain the candidate's authentic voice

Return JSON:
{
  "tailored_resume_text": "string",  // Full tailored resume as plain text
  "modifications": [
    {
      "field": "string",            // e.g., "skills", "experience[0].description"
      "original": "string",
      "modified": "string",
      "reason": "string"
    }
  ],
  "ats_keywords_added": ["string"],
  "estimated_ats_score": number     // 0-100
}
```

---

### 1.3 Cover Letter Generation Prompt

```
SYSTEM:
You are a professional career coach specialising in cover letter writing. Write a compelling, 
personalised cover letter that matches the tone and seniority of the role.

USER:
Candidate Information:
- Name: {candidate_name}
- Current Role: {current_title} at {current_company}
- Years of Experience: {years_experience}
- Key Skills: {top_skills}
- Location: {location}

Target Job:
- Role: {job_title}
- Company: {company_name}
- Location: {job_location}
- Seniority: {seniority_level}

Job Requirements Summary:
{jd_summary}

Instructions:
1. Opening: Reference the specific role and express genuine interest in {company_name}
2. Body paragraph 1: Highlight 2-3 most relevant experiences with quantified results
3. Body paragraph 2: Demonstrate knowledge of {company_name}'s work/mission
4. Closing: Clear call to action for interview
5. Tone: {seniority_level == SENIOR ? "Senior, confident, strategic" : "Enthusiastic, growth-oriented"}
6. Length: 250-350 words

Return ONLY the cover letter text, no JSON, no labels.
```

---

## 2. Match Score Algorithm (Pseudocode)

```python
def compute_match_score(user: User, resume: Resume, job: Job, analysis: JobAnalysis) -> MatchScore:
    
    # ── Skill Score (50%) ──────────────────────────────────────────
    resume_skills = normalise_skills(resume.skills_json)
    required_skills = {s.skill_name.lower() for s in job.job_skills if s.is_required}
    optional_skills = {s.skill_name.lower() for s in job.job_skills if not s.is_required}
    
    matched_required = required_skills & resume_skills
    matched_optional = optional_skills & resume_skills
    
    if len(required_skills) == 0:
        skill_score = 50.0  # No required skills → neutral score
    else:
        base_skill_score = (len(matched_required) / len(required_skills)) * 100
        bonus = min(10.0, len(matched_optional) * 2.0)
        skill_score = min(100.0, base_skill_score + bonus)
    
    # ── Experience Score (25%) ─────────────────────────────────────
    user_years = user.experience_years
    exp_min = analysis.experience_min or 0
    exp_max = analysis.experience_max or 99
    
    if exp_min <= user_years <= exp_max + 2:
        experience_score = 100.0
    elif user_years < exp_min:
        gap = exp_min - user_years
        experience_score = max(0.0, 100.0 - (gap * 20.0))
    else:  # overqualified
        experience_score = 80.0
    
    # ── Location Score (15%) ───────────────────────────────────────
    if analysis.remote_type == "REMOTE":
        location_score = 100.0
    elif normalise_city(job.location) == normalise_city(user.location):
        location_score = 100.0
    elif same_state(job.location, user.location):
        location_score = 60.0
    elif user_preferences.remote_ok and analysis.remote_type == "HYBRID":
        location_score = 80.0
    else:
        location_score = 0.0
    
    # ── Domain Score (10%) ─────────────────────────────────────────
    if analysis.domain in user_preferences.industries:
        domain_score = 100.0
    elif analysis.domain in infer_domains_from_experience(resume):
        domain_score = 70.0
    else:
        domain_score = 30.0
    
    # ── Overall Score ──────────────────────────────────────────────
    overall_score = (
        skill_score      * 0.50 +
        experience_score * 0.25 +
        location_score   * 0.15 +
        domain_score     * 0.10
    )
    
    return MatchScore(
        overall_score=round(overall_score, 2),
        skill_score=round(skill_score, 2),
        experience_score=round(experience_score, 2),
        location_score=round(location_score, 2),
        domain_score=round(domain_score, 2)
    )
```

---

## 3. Security Architecture

### 3.1 Authentication Flow

```
Client                    Nginx                 Spring Boot            Redis/DB
  │                          │                       │                     │
  │  POST /auth/login        │                       │                     │
  │─────────────────────────▶│                       │                     │
  │                          │ Forward request       │                     │
  │                          │──────────────────────▶│                     │
  │                          │                       │ Validate credentials│
  │                          │                       │────────────────────▶│
  │                          │                       │ User found + valid  │
  │                          │                       │◀────────────────────│
  │                          │                       │ Generate access JWT │
  │                          │                       │ Generate refresh JWT│
  │                          │                       │ Store refresh JTI   │
  │                          │                       │ in Redis (7d TTL)   │
  │                          │                       │────────────────────▶│
  │  200 {accessToken,       │                       │                     │
  │       refreshToken}      │                       │                     │
  │◀─────────────────────────│◀──────────────────────│                     │
```

### 3.2 Request Authorisation Flow

```
Incoming Request
      │
      ▼
JwtAuthFilter.doFilterInternal()
      │
      ├── Extract "Authorization: Bearer <token>"
      │
      ├── Validate signature with HMAC-SHA256 secret
      │
      ├── Check token not in Redis blocklist
      │
      ├── Check expiry (exp claim)
      │
      ├── Load UserDetails from DB (or cache)
      │
      ├── Set SecurityContextHolder
      │
      └── continue chain → Controller
```

### 3.3 Data Encryption

```
At Rest (MySQL):
  - PII fields (email, phone, full_name) → AES-256-GCM with application-managed key
  - Passwords → BCrypt (strength 12)
  - Files → Volume encryption at OS level

In Transit:
  - TLS 1.3 enforced at Nginx
  - HSTS header: max-age=31536000; includeSubDomains
  - Cipher suites: TLS_AES_256_GCM_SHA384, TLS_CHACHA20_POLY1305_SHA256
```

---

## 4. Error Handling Standard

### 4.1 GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, 
                                                        HttpServletRequest req) {
        return ResponseEntity.status(404).body(ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(404)
            .error("Not Found")
            .code("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .path(req.getRequestURI())
            .traceId(MDC.get("traceId"))
            .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
            .toList();

        return ResponseEntity.status(400).body(ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(400)
            .error("Bad Request")
            .code("VALIDATION_FAILED")
            .message("Validation failed for " + fieldErrors.size() + " field(s)")
            .errors(fieldErrors)
            .path(req.getRequestURI())
            .traceId(MDC.get("traceId"))
            .build());
    }
}
```

### 4.2 Retry Strategy for AI Calls

```java
@Bean
public RetryTemplate aiRetryTemplate() {
    return RetryTemplate.builder()
        .maxAttempts(3)
        .exponentialBackoff(1000, 2, 8000)  // 1s, 2s, 4s
        .retryOn(OpenAiRateLimitException.class)
        .retryOn(OpenAiTimeoutException.class)
        .withListener(new AiRetryListener())  // logs each attempt
        .build();
}
```

### 4.3 RabbitMQ Dead Letter Queue Strategy

```
jobs.raw ──────────────────────────────▶ jobs.raw.consumer
    │                                         │ (3 attempts)
    │                                         │ on failure:
    │                                         ▼
    │                                   jobs.raw.dlq
    │                                         │
    │                                   (manual review
    │                                    or replay after
    │                                    issue resolved)
```

---

*Document End — Technical Specifications v1.0*
