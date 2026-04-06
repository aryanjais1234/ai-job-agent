# Database Schema
## AI Job Agent — Automated Job Application System
**Version:** 1.0  
**Database:** MySQL 8.0  
**Character Set:** utf8mb4  
**Collation:** utf8mb4_unicode_ci

---

## ER Diagram (Text-Based)

```
users ──────────────┬──── user_preferences (1:1)
  │                 │
  │                 ├──── resumes (1:N)
  │                 │         │
  │                 │         ├── resume_skills (1:N)
  │                 │         ├── resume_experiences (1:N)
  │                 │         └── resume_educations (1:N)
  │                 │
  │                 ├──── job_matches (1:N) ──── jobs (N:1)
  │                 │                              │
  │                 │                              ├── job_analyses (1:1)
  │                 │                              └── job_skills (1:N)
  │                 │
  │                 ├──── tailored_resumes (1:N)
  │                 ├──── cover_letters (1:N)
  │                 ├──── applications (1:N)
  │                 ├──── notification_preferences (1:1)
  │                 ├──── notification_logs (1:N)
  │                 └──── ai_usage_logs (1:N)
  │
scrape_logs (standalone audit log)
ai_usage_logs (references users)
```

---

## Table Definitions

### 1. users

```sql
CREATE TABLE users (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    full_name           VARCHAR(255) NOT NULL,
    phone               VARCHAR(20),
    linkedin_url        VARCHAR(500),
    github_url          VARCHAR(500),
    portfolio_url       VARCHAR(500),
    location            VARCHAR(255),
    experience_years    TINYINT UNSIGNED DEFAULT 0,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified      BOOLEAN NOT NULL DEFAULT FALSE,
    verification_token  VARCHAR(255),
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Column Notes:**
- `password_hash`: BCrypt hash, strength 12
- `verification_token`: UUID, nulled after email verification
- `experience_years`: 0–50 range enforced at application layer

---

### 2. user_preferences

```sql
CREATE TABLE user_preferences (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL UNIQUE,
    job_titles          JSON,           -- ["Software Engineer", "Backend Developer"]
    locations           JSON,           -- ["Bangalore", "Remote", "Mumbai"]
    min_salary          DECIMAL(10,2),
    max_salary          DECIMAL(10,2),
    job_types           JSON,           -- ["FULL_TIME", "CONTRACT"]
    remote_ok           BOOLEAN DEFAULT TRUE,
    experience_levels   JSON,           -- ["MID", "SENIOR"]
    skills_required     JSON,           -- ["Java", "Spring Boot"]
    industries          JSON,           -- ["FinTech", "E-Commerce"]
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 3. resumes

```sql
CREATE TABLE resumes (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL,
    original_filename   VARCHAR(255) NOT NULL,
    file_path           VARCHAR(500) NOT NULL,
    parsed_text         LONGTEXT,
    skills_json         JSON,
    experience_summary  TEXT,
    education_summary   TEXT,
    is_primary          BOOLEAN NOT NULL DEFAULT FALSE,
    file_size_bytes     INT UNSIGNED,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_primary (user_id, is_primary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 4. resume_skills

```sql
CREATE TABLE resume_skills (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    resume_id           BIGINT UNSIGNED NOT NULL,
    skill_name          VARCHAR(100) NOT NULL,
    skill_category      ENUM('TECHNICAL', 'SOFT', 'DOMAIN', 'TOOL', 'LANGUAGE') NOT NULL,
    proficiency_level   ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') DEFAULT 'INTERMEDIATE',
    years_experience    DECIMAL(4,1),
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    INDEX idx_resume_skill (resume_id, skill_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 5. resume_experiences

```sql
CREATE TABLE resume_experiences (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    resume_id           BIGINT UNSIGNED NOT NULL,
    company             VARCHAR(255) NOT NULL,
    title               VARCHAR(255) NOT NULL,
    start_date          DATE,
    end_date            DATE,
    description         TEXT,
    is_current          BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    INDEX idx_resume_exp (resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 6. resume_educations

```sql
CREATE TABLE resume_educations (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    resume_id           BIGINT UNSIGNED NOT NULL,
    institution         VARCHAR(255) NOT NULL,
    degree              VARCHAR(100),
    field_of_study      VARCHAR(255),
    graduation_year     YEAR,
    gpa                 DECIMAL(3,2),
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    INDEX idx_resume_edu (resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 7. jobs

```sql
CREATE TABLE jobs (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    company             VARCHAR(255) NOT NULL,
    location            VARCHAR(255),
    job_type            ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE'),
    experience_required VARCHAR(50),
    salary_min          DECIMAL(10,2),
    salary_max          DECIMAL(10,2),
    description         LONGTEXT,
    requirements        TEXT,
    skills_required     JSON,
    source_platform     ENUM('INDEED', 'LINKEDIN', 'NAUKRI') NOT NULL,
    source_url          VARCHAR(1000) NOT NULL,
    source_job_id       VARCHAR(255),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    posted_at           DATETIME,
    scraped_at          DATETIME NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_source (source_platform, source_url(255)),
    INDEX idx_is_active (is_active),
    INDEX idx_scraped_at (scraped_at),
    INDEX idx_posted_at (posted_at),
    FULLTEXT INDEX ft_title_company (title, company)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 8. job_analyses

```sql
CREATE TABLE job_analyses (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    job_id              BIGINT UNSIGNED NOT NULL UNIQUE,
    required_skills     JSON,           -- ["Java", "Spring Boot", "MySQL"]
    nice_to_have_skills JSON,           -- ["Kotlin", "Kubernetes"]
    experience_min      TINYINT UNSIGNED,
    experience_max      TINYINT UNSIGNED,
    education_required  VARCHAR(100),
    domain              VARCHAR(100),
    keywords            JSON,           -- ATS keywords extracted
    seniority_level     ENUM('INTERN', 'JUNIOR', 'MID', 'SENIOR', 'LEAD', 'MANAGER', 'DIRECTOR'),
    remote_type         ENUM('REMOTE', 'HYBRID', 'ON_SITE'),
    analyzed_at         DATETIME NOT NULL,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 9. job_skills

```sql
CREATE TABLE job_skills (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    job_id              BIGINT UNSIGNED NOT NULL,
    skill_name          VARCHAR(100) NOT NULL,
    skill_category      ENUM('TECHNICAL', 'SOFT', 'DOMAIN', 'TOOL', 'LANGUAGE') NOT NULL,
    is_required         BOOLEAN NOT NULL DEFAULT TRUE,
    importance_score    TINYINT UNSIGNED DEFAULT 50,  -- 0-100
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    INDEX idx_job_skill (job_id, is_required)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 10. job_matches

```sql
CREATE TABLE job_matches (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL,
    resume_id           BIGINT UNSIGNED NOT NULL,
    job_id              BIGINT UNSIGNED NOT NULL,
    overall_score       DECIMAL(5,2) NOT NULL,
    skill_score         DECIMAL(5,2) NOT NULL,
    experience_score    DECIMAL(5,2) NOT NULL,
    location_score      DECIMAL(5,2) NOT NULL,
    domain_score        DECIMAL(5,2) NOT NULL,
    status              ENUM('NEW', 'VIEWED', 'TAILORING', 'READY', 'APPLIED', 'DISMISSED') DEFAULT 'NEW',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_job (user_id, job_id),
    INDEX idx_user_score (user_id, overall_score DESC),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 11. tailored_resumes

```sql
CREATE TABLE tailored_resumes (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL,
    resume_id           BIGINT UNSIGNED NOT NULL,
    job_id              BIGINT UNSIGNED NOT NULL,
    tailored_content    LONGTEXT NOT NULL,
    modifications_log   JSON,           -- [{field, original, modified, reason}]
    ats_score           DECIMAL(5,2),
    pdf_file_path       VARCHAR(500),
    is_downloaded       BOOLEAN DEFAULT FALSE,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_job_resume (user_id, job_id, resume_id),
    INDEX idx_user_tailored (user_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 12. cover_letters

```sql
CREATE TABLE cover_letters (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL,
    job_id              BIGINT UNSIGNED NOT NULL,
    content             LONGTEXT NOT NULL,
    pdf_file_path       VARCHAR(500),
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    INDEX idx_user_cl (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 13. applications

```sql
CREATE TABLE applications (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL,
    job_id              BIGINT UNSIGNED NOT NULL,
    resume_id           BIGINT UNSIGNED,
    tailored_resume_id  BIGINT UNSIGNED,
    cover_letter_id     BIGINT UNSIGNED,
    status              ENUM('PENDING', 'APPLIED', 'INTERVIEW', 'OFFER', 'REJECTED', 'WITHDRAWN') NOT NULL DEFAULT 'PENDING',
    applied_at          DATETIME,
    response_date       DATETIME,
    notes               TEXT,
    interview_date      DATETIME,
    rejection_reason    VARCHAR(500),
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE SET NULL,
    FOREIGN KEY (tailored_resume_id) REFERENCES tailored_resumes(id) ON DELETE SET NULL,
    FOREIGN KEY (cover_letter_id) REFERENCES cover_letters(id) ON DELETE SET NULL,
    UNIQUE KEY uk_user_job (user_id, job_id),
    INDEX idx_user_status (user_id, status),
    INDEX idx_applied_at (applied_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 14. notification_preferences

```sql
CREATE TABLE notification_preferences (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL UNIQUE,
    email_enabled       BOOLEAN DEFAULT TRUE,
    push_enabled        BOOLEAN DEFAULT FALSE,
    daily_digest        BOOLEAN DEFAULT TRUE,
    weekly_report       BOOLEAN DEFAULT TRUE,
    match_threshold     TINYINT UNSIGNED DEFAULT 70,   -- Notify only above this score
    new_job_alerts      BOOLEAN DEFAULT TRUE,
    application_updates BOOLEAN DEFAULT TRUE,
    digest_time         TIME DEFAULT '07:00:00',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 15. notification_logs

```sql
CREATE TABLE notification_logs (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL,
    notification_type   ENUM('DAILY_DIGEST', 'MATCH_ALERT', 'APPLICATION_UPDATE', 'WEEKLY_REPORT', 'SYSTEM') NOT NULL,
    subject             VARCHAR(500),
    content             TEXT,
    sent_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read             BOOLEAN DEFAULT FALSE,
    read_at             DATETIME,
    metadata_json       JSON,           -- {matchIds: [], applicationId: null}
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_notif (user_id, sent_at DESC),
    INDEX idx_is_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 16. scrape_logs

```sql
CREATE TABLE scrape_logs (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    platform            ENUM('INDEED', 'LINKEDIN', 'NAUKRI', 'ALL') NOT NULL,
    status              ENUM('RUNNING', 'COMPLETED', 'FAILED', 'PARTIAL') NOT NULL,
    jobs_found          INT UNSIGNED DEFAULT 0,
    jobs_new            INT UNSIGNED DEFAULT 0,
    jobs_updated        INT UNSIGNED DEFAULT 0,
    error_message       TEXT,
    started_at          DATETIME NOT NULL,
    completed_at        DATETIME,
    INDEX idx_platform_started (platform, started_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 17. ai_usage_logs

```sql
CREATE TABLE ai_usage_logs (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED,  -- NULL for system-initiated operations
    operation_type      ENUM('JD_ANALYSIS', 'RESUME_TAILOR', 'COVER_LETTER', 'SKILL_EXTRACT') NOT NULL,
    model_used          VARCHAR(50) NOT NULL,
    prompt_tokens       INT UNSIGNED NOT NULL DEFAULT 0,
    completion_tokens   INT UNSIGNED NOT NULL DEFAULT 0,
    total_tokens        INT UNSIGNED NOT NULL DEFAULT 0,
    cost_usd            DECIMAL(10,6),
    duration_ms         INT UNSIGNED,
    success             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_ai (user_id, created_at DESC),
    INDEX idx_operation (operation_type, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Index Summary

| Table | Indexes |
|-------|---------|
| users | PK, UNIQUE(email), idx_is_active |
| resumes | PK, FK(user_id), idx_user_primary |
| jobs | PK, UNIQUE(source_platform, source_url), idx_is_active, idx_scraped_at, FULLTEXT |
| job_matches | PK, UNIQUE(user_id, job_id), idx_user_score, idx_status |
| applications | PK, UNIQUE(user_id, job_id), idx_user_status, idx_applied_at |
| ai_usage_logs | PK, FK(user_id), idx_user_ai, idx_operation |

---

*Document End — Database Schema v1.0*
