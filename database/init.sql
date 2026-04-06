-- ============================================================
-- AI Job Agent — Database Initialisation Script
-- Version: 1.0
-- Database: MySQL 8.0
-- Character Set: utf8mb4 / utf8mb4_unicode_ci
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET time_zone = '+00:00';

CREATE DATABASE IF NOT EXISTS `aijobagent`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `aijobagent`;

-- ============================================================
-- 1. users
-- ============================================================
CREATE TABLE IF NOT EXISTS `users` (
    `id`                 BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT,
    `email`              VARCHAR(255)       NOT NULL,
    `password_hash`      VARCHAR(255)       NOT NULL,
    `full_name`          VARCHAR(255)       NOT NULL,
    `phone`              VARCHAR(20)        DEFAULT NULL,
    `linkedin_url`       VARCHAR(500)       DEFAULT NULL,
    `github_url`         VARCHAR(500)       DEFAULT NULL,
    `portfolio_url`      VARCHAR(500)       DEFAULT NULL,
    `location`           VARCHAR(255)       DEFAULT NULL,
    `experience_years`   TINYINT UNSIGNED   NOT NULL DEFAULT 0,
    `is_active`          BOOLEAN            NOT NULL DEFAULT TRUE,
    `email_verified`     BOOLEAN            NOT NULL DEFAULT FALSE,
    `verification_token` VARCHAR(255)       DEFAULT NULL,
    `created_at`         DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. user_preferences
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_preferences` (
    `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `user_id`           BIGINT UNSIGNED  NOT NULL,
    `job_titles`        JSON             DEFAULT NULL,
    `locations`         JSON             DEFAULT NULL,
    `min_salary`        DECIMAL(10,2)    DEFAULT NULL,
    `max_salary`        DECIMAL(10,2)    DEFAULT NULL,
    `job_types`         JSON             DEFAULT NULL,
    `remote_ok`         BOOLEAN          NOT NULL DEFAULT TRUE,
    `experience_levels` JSON             DEFAULT NULL,
    `skills_required`   JSON             DEFAULT NULL,
    `industries`        JSON             DEFAULT NULL,
    `created_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_pref` (`user_id`),
    CONSTRAINT `fk_pref_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. resumes
-- ============================================================
CREATE TABLE IF NOT EXISTS `resumes` (
    `id`                 BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT UNSIGNED  NOT NULL,
    `original_filename`  VARCHAR(255)     NOT NULL,
    `file_path`          VARCHAR(500)     NOT NULL,
    `parsed_text`        LONGTEXT         DEFAULT NULL,
    `skills_json`        JSON             DEFAULT NULL,
    `experience_summary` TEXT             DEFAULT NULL,
    `education_summary`  TEXT             DEFAULT NULL,
    `is_primary`         BOOLEAN          NOT NULL DEFAULT FALSE,
    `file_size_bytes`    INT UNSIGNED     DEFAULT NULL,
    `created_at`         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_resume_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_user_primary` (`user_id`, `is_primary`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. resume_skills
-- ============================================================
CREATE TABLE IF NOT EXISTS `resume_skills` (
    `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `resume_id`         BIGINT UNSIGNED  NOT NULL,
    `skill_name`        VARCHAR(100)     NOT NULL,
    `skill_category`    ENUM('TECHNICAL','SOFT','DOMAIN','TOOL','LANGUAGE') NOT NULL,
    `proficiency_level` ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT') DEFAULT 'INTERMEDIATE',
    `years_experience`  DECIMAL(4,1)     DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_rskill_resume` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE,
    INDEX `idx_resume_skill` (`resume_id`, `skill_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. resume_experiences
-- ============================================================
CREATE TABLE IF NOT EXISTS `resume_experiences` (
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `resume_id`   BIGINT UNSIGNED  NOT NULL,
    `company`     VARCHAR(255)     NOT NULL,
    `title`       VARCHAR(255)     NOT NULL,
    `start_date`  DATE             DEFAULT NULL,
    `end_date`    DATE             DEFAULT NULL,
    `description` TEXT             DEFAULT NULL,
    `is_current`  BOOLEAN          NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_rexp_resume` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE,
    INDEX `idx_resume_exp` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. resume_educations
-- ============================================================
CREATE TABLE IF NOT EXISTS `resume_educations` (
    `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `resume_id`       BIGINT UNSIGNED  NOT NULL,
    `institution`     VARCHAR(255)     NOT NULL,
    `degree`          VARCHAR(100)     DEFAULT NULL,
    `field_of_study`  VARCHAR(255)     DEFAULT NULL,
    `graduation_year` YEAR             DEFAULT NULL,
    `gpa`             DECIMAL(3,2)     DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_redu_resume` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE,
    INDEX `idx_resume_edu` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. jobs
-- ============================================================
CREATE TABLE IF NOT EXISTS `jobs` (
    `id`                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `title`               VARCHAR(255)     NOT NULL,
    `company`             VARCHAR(255)     NOT NULL,
    `location`            VARCHAR(255)     DEFAULT NULL,
    `job_type`            ENUM('FULL_TIME','PART_TIME','CONTRACT','INTERNSHIP','FREELANCE') DEFAULT NULL,
    `experience_required` VARCHAR(50)      DEFAULT NULL,
    `salary_min`          DECIMAL(10,2)    DEFAULT NULL,
    `salary_max`          DECIMAL(10,2)    DEFAULT NULL,
    `description`         LONGTEXT         DEFAULT NULL,
    `requirements`        TEXT             DEFAULT NULL,
    `skills_required`     JSON             DEFAULT NULL,
    `source_platform`     ENUM('INDEED','LINKEDIN','NAUKRI') NOT NULL,
    `source_url`          VARCHAR(1000)    NOT NULL,
    `source_job_id`       VARCHAR(255)     DEFAULT NULL,
    `is_active`           BOOLEAN          NOT NULL DEFAULT TRUE,
    `posted_at`           DATETIME         DEFAULT NULL,
    `scraped_at`          DATETIME         NOT NULL,
    `created_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_source` (`source_platform`, `source_url`(255)),
    INDEX `idx_is_active` (`is_active`),
    INDEX `idx_scraped_at` (`scraped_at`),
    INDEX `idx_posted_at` (`posted_at`),
    FULLTEXT INDEX `ft_title_company` (`title`, `company`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. job_analyses
-- ============================================================
CREATE TABLE IF NOT EXISTS `job_analyses` (
    `id`                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `job_id`              BIGINT UNSIGNED  NOT NULL,
    `required_skills`     JSON             DEFAULT NULL,
    `nice_to_have_skills` JSON             DEFAULT NULL,
    `experience_min`      TINYINT UNSIGNED DEFAULT NULL,
    `experience_max`      TINYINT UNSIGNED DEFAULT NULL,
    `education_required`  VARCHAR(100)     DEFAULT NULL,
    `domain`              VARCHAR(100)     DEFAULT NULL,
    `keywords`            JSON             DEFAULT NULL,
    `seniority_level`     ENUM('INTERN','JUNIOR','MID','SENIOR','LEAD','MANAGER','DIRECTOR') DEFAULT NULL,
    `remote_type`         ENUM('REMOTE','HYBRID','ON_SITE') DEFAULT NULL,
    `analyzed_at`         DATETIME         NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_job_analysis` (`job_id`),
    CONSTRAINT `fk_analysis_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9. job_skills
-- ============================================================
CREATE TABLE IF NOT EXISTS `job_skills` (
    `id`               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `job_id`           BIGINT UNSIGNED  NOT NULL,
    `skill_name`       VARCHAR(100)     NOT NULL,
    `skill_category`   ENUM('TECHNICAL','SOFT','DOMAIN','TOOL','LANGUAGE') NOT NULL,
    `is_required`      BOOLEAN          NOT NULL DEFAULT TRUE,
    `importance_score` TINYINT UNSIGNED NOT NULL DEFAULT 50,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_jskill_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE,
    INDEX `idx_job_skill` (`job_id`, `is_required`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. job_matches
-- ============================================================
CREATE TABLE IF NOT EXISTS `job_matches` (
    `id`               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT UNSIGNED  NOT NULL,
    `resume_id`        BIGINT UNSIGNED  NOT NULL,
    `job_id`           BIGINT UNSIGNED  NOT NULL,
    `overall_score`    DECIMAL(5,2)     NOT NULL,
    `skill_score`      DECIMAL(5,2)     NOT NULL,
    `experience_score` DECIMAL(5,2)     NOT NULL,
    `location_score`   DECIMAL(5,2)     NOT NULL,
    `domain_score`     DECIMAL(5,2)     NOT NULL,
    `status`           ENUM('NEW','VIEWED','TAILORING','READY','APPLIED','DISMISSED') NOT NULL DEFAULT 'NEW',
    `created_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_job_match` (`user_id`, `job_id`),
    CONSTRAINT `fk_match_user`   FOREIGN KEY (`user_id`)   REFERENCES `users`   (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_match_resume` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_match_job`    FOREIGN KEY (`job_id`)    REFERENCES `jobs`    (`id`) ON DELETE CASCADE,
    INDEX `idx_user_score` (`user_id`, `overall_score`),
    INDEX `idx_match_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 11. tailored_resumes
-- ============================================================
CREATE TABLE IF NOT EXISTS `tailored_resumes` (
    `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `user_id`           BIGINT UNSIGNED  NOT NULL,
    `resume_id`         BIGINT UNSIGNED  NOT NULL,
    `job_id`            BIGINT UNSIGNED  NOT NULL,
    `tailored_content`  LONGTEXT         NOT NULL,
    `modifications_log` JSON             DEFAULT NULL,
    `ats_score`         DECIMAL(5,2)     DEFAULT NULL,
    `pdf_file_path`     VARCHAR(500)     DEFAULT NULL,
    `is_downloaded`     BOOLEAN          NOT NULL DEFAULT FALSE,
    `created_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tailored` (`user_id`, `job_id`, `resume_id`),
    CONSTRAINT `fk_tailored_user`   FOREIGN KEY (`user_id`)   REFERENCES `users`   (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_tailored_resume` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_tailored_job`    FOREIGN KEY (`job_id`)    REFERENCES `jobs`    (`id`) ON DELETE CASCADE,
    INDEX `idx_user_tailored` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 12. cover_letters
-- ============================================================
CREATE TABLE IF NOT EXISTS `cover_letters` (
    `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT UNSIGNED  NOT NULL,
    `job_id`        BIGINT UNSIGNED  NOT NULL,
    `content`       LONGTEXT         NOT NULL,
    `pdf_file_path` VARCHAR(500)     DEFAULT NULL,
    `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_cl_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cl_job`  FOREIGN KEY (`job_id`)  REFERENCES `jobs`  (`id`) ON DELETE CASCADE,
    INDEX `idx_user_cl` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 13. applications
-- ============================================================
CREATE TABLE IF NOT EXISTS `applications` (
    `id`                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `user_id`             BIGINT UNSIGNED  NOT NULL,
    `job_id`              BIGINT UNSIGNED  NOT NULL,
    `resume_id`           BIGINT UNSIGNED  DEFAULT NULL,
    `tailored_resume_id`  BIGINT UNSIGNED  DEFAULT NULL,
    `cover_letter_id`     BIGINT UNSIGNED  DEFAULT NULL,
    `status`              ENUM('PENDING','APPLIED','INTERVIEW','OFFER','REJECTED','WITHDRAWN') NOT NULL DEFAULT 'PENDING',
    `applied_at`          DATETIME         DEFAULT NULL,
    `response_date`       DATETIME         DEFAULT NULL,
    `notes`               TEXT             DEFAULT NULL,
    `interview_date`      DATETIME         DEFAULT NULL,
    `rejection_reason`    VARCHAR(500)     DEFAULT NULL,
    `created_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_job_app` (`user_id`, `job_id`),
    CONSTRAINT `fk_app_user`     FOREIGN KEY (`user_id`)            REFERENCES `users`           (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_app_job`      FOREIGN KEY (`job_id`)             REFERENCES `jobs`            (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_app_resume`   FOREIGN KEY (`resume_id`)          REFERENCES `resumes`         (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_app_tailored` FOREIGN KEY (`tailored_resume_id`) REFERENCES `tailored_resumes`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_app_cl`       FOREIGN KEY (`cover_letter_id`)    REFERENCES `cover_letters`   (`id`) ON DELETE SET NULL,
    INDEX `idx_user_app_status` (`user_id`, `status`),
    INDEX `idx_applied_at` (`applied_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 14. notification_preferences
-- ============================================================
CREATE TABLE IF NOT EXISTS `notification_preferences` (
    `id`                 BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT UNSIGNED  NOT NULL,
    `email_enabled`      BOOLEAN          NOT NULL DEFAULT TRUE,
    `push_enabled`       BOOLEAN          NOT NULL DEFAULT FALSE,
    `daily_digest`       BOOLEAN          NOT NULL DEFAULT TRUE,
    `weekly_report`      BOOLEAN          NOT NULL DEFAULT TRUE,
    `match_threshold`    TINYINT UNSIGNED NOT NULL DEFAULT 70,
    `new_job_alerts`     BOOLEAN          NOT NULL DEFAULT TRUE,
    `application_updates` BOOLEAN         NOT NULL DEFAULT TRUE,
    `digest_time`        TIME             NOT NULL DEFAULT '07:00:00',
    `created_at`         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notif_pref_user` (`user_id`),
    CONSTRAINT `fk_nfpref_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 15. notification_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS `notification_logs` (
    `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `user_id`           BIGINT UNSIGNED  NOT NULL,
    `notification_type` ENUM('DAILY_DIGEST','MATCH_ALERT','APPLICATION_UPDATE','WEEKLY_REPORT','SYSTEM') NOT NULL,
    `subject`           VARCHAR(500)     DEFAULT NULL,
    `content`           TEXT             DEFAULT NULL,
    `sent_at`           DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_read`           BOOLEAN          NOT NULL DEFAULT FALSE,
    `read_at`           DATETIME         DEFAULT NULL,
    `metadata_json`     JSON             DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_nflog_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_user_notif_log` (`user_id`, `sent_at`),
    INDEX `idx_notif_is_read`  (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 16. scrape_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS `scrape_logs` (
    `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `platform`      ENUM('INDEED','LINKEDIN','NAUKRI','ALL') NOT NULL,
    `status`        ENUM('RUNNING','COMPLETED','FAILED','PARTIAL') NOT NULL,
    `jobs_found`    INT UNSIGNED     NOT NULL DEFAULT 0,
    `jobs_new`      INT UNSIGNED     NOT NULL DEFAULT 0,
    `jobs_updated`  INT UNSIGNED     NOT NULL DEFAULT 0,
    `error_message` TEXT             DEFAULT NULL,
    `started_at`    DATETIME         NOT NULL,
    `completed_at`  DATETIME         DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_scrape_platform` (`platform`, `started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 17. ai_usage_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_usage_logs` (
    `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `user_id`           BIGINT UNSIGNED  DEFAULT NULL,
    `operation_type`    ENUM('JD_ANALYSIS','RESUME_TAILOR','COVER_LETTER','SKILL_EXTRACT') NOT NULL,
    `model_used`        VARCHAR(50)      NOT NULL,
    `prompt_tokens`     INT UNSIGNED     NOT NULL DEFAULT 0,
    `completion_tokens` INT UNSIGNED     NOT NULL DEFAULT 0,
    `total_tokens`      INT UNSIGNED     NOT NULL DEFAULT 0,
    `cost_usd`          DECIMAL(10,6)    DEFAULT NULL,
    `duration_ms`       INT UNSIGNED     DEFAULT NULL,
    `success`           BOOLEAN          NOT NULL DEFAULT TRUE,
    `created_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_ai_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    INDEX `idx_ai_user_log`   (`user_id`, `created_at`),
    INDEX `idx_ai_operation`  (`operation_type`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Seed Data: Default Notification Preferences trigger
-- ============================================================
DELIMITER $$
CREATE TRIGGER `after_user_insert`
AFTER INSERT ON `users`
FOR EACH ROW
BEGIN
    INSERT INTO `notification_preferences` (`user_id`)
    VALUES (NEW.id);
END$$
DELIMITER ;

-- ============================================================
-- Done
-- ============================================================
