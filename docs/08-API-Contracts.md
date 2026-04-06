# API Contracts
## AI Job Agent — Automated Job Application System
**Version:** 1.0  
**Base URL:** `https://api.aijobagent.dev/api/v1`  
**Content-Type:** `application/json`

---

## Authentication

All protected endpoints require:
```
Authorization: Bearer <access_token>
```

---

## 1. Auth Endpoints

### POST /auth/register

**Request:**
```json
{
  "fullName": "Priya Sharma",
  "email": "priya@example.com",
  "password": "SecurePass123!"
}
```

**Response 201:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "email": "priya@example.com",
    "fullName": "Priya Sharma",
    "emailVerified": false
  }
}
```

**Error 409:**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 409,
  "code": "DUPLICATE_RESOURCE",
  "message": "Email priya@example.com is already registered"
}
```

---

### POST /auth/login

**Request:**
```json
{
  "email": "priya@example.com",
  "password": "SecurePass123!"
}
```

**Response 200:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "email": "priya@example.com",
    "fullName": "Priya Sharma",
    "emailVerified": true
  }
}
```

---

## 2. User Endpoints

### GET /users/profile

**Response 200:**
```json
{
  "id": 1,
  "email": "priya@example.com",
  "fullName": "Priya Sharma",
  "phone": "+91 98765 43210",
  "linkedinUrl": "https://linkedin.com/in/priya-sharma",
  "githubUrl": "https://github.com/priyasharma",
  "portfolioUrl": "https://priya.dev",
  "location": "Bangalore, Karnataka",
  "experienceYears": 3,
  "isActive": true,
  "emailVerified": true,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

### PUT /users/preferences

**Request:**
```json
{
  "jobTitles": ["Software Engineer", "Backend Developer", "Java Developer"],
  "locations": ["Bangalore", "Remote", "Hyderabad"],
  "minSalary": 1200000,
  "maxSalary": 2500000,
  "jobTypes": ["FULL_TIME"],
  "remoteOk": true,
  "experienceLevels": ["MID", "SENIOR"],
  "skillsRequired": ["Java", "Spring Boot", "MySQL"],
  "industries": ["FinTech", "E-Commerce", "SaaS"]
}
```

**Response 200:**
```json
{
  "id": 1,
  "userId": 1,
  "jobTitles": ["Software Engineer", "Backend Developer", "Java Developer"],
  "locations": ["Bangalore", "Remote", "Hyderabad"],
  "minSalary": 1200000.00,
  "maxSalary": 2500000.00,
  "jobTypes": ["FULL_TIME"],
  "remoteOk": true,
  "experienceLevels": ["MID", "SENIOR"],
  "skillsRequired": ["Java", "Spring Boot", "MySQL"],
  "industries": ["FinTech", "E-Commerce", "SaaS"]
}
```

---

## 3. Resume Endpoints

### POST /resumes (Multipart)

**Request:** `Content-Type: multipart/form-data`
```
file: <binary PDF/DOCX>
```

**Response 201:**
```json
{
  "id": 5,
  "userId": 1,
  "originalFilename": "priya_resume_2024.pdf",
  "fileSizeBytes": 245760,
  "isPrimary": true,
  "parsedData": {
    "skills": [
      {"name": "Java", "category": "TECHNICAL", "proficiencyLevel": "ADVANCED"},
      {"name": "Spring Boot", "category": "TECHNICAL", "proficiencyLevel": "ADVANCED"},
      {"name": "MySQL", "category": "TOOL", "proficiencyLevel": "INTERMEDIATE"}
    ],
    "experiences": [
      {
        "company": "TCS",
        "title": "Software Engineer",
        "startDate": "2021-06-01",
        "endDate": null,
        "isCurrent": true,
        "description": "Built microservices using Spring Boot..."
      }
    ],
    "educations": [
      {
        "institution": "VTU",
        "degree": "B.E.",
        "fieldOfStudy": "Computer Science",
        "graduationYear": 2021,
        "gpa": 8.7
      }
    ]
  },
  "createdAt": "2024-01-15T10:00:00Z"
}
```

---

## 4. Job & Match Endpoints

### GET /jobs/matches?minScore=70&page=0&size=10

**Response 200:**
```json
{
  "content": [
    {
      "matchId": 101,
      "overallScore": 94.5,
      "skillScore": 96.0,
      "experienceScore": 100.0,
      "locationScore": 100.0,
      "domainScore": 70.0,
      "status": "NEW",
      "job": {
        "id": 42,
        "title": "Senior Java Developer",
        "company": "Infosys",
        "location": "Bangalore",
        "jobType": "FULL_TIME",
        "salaryMin": 1500000,
        "salaryMax": 2200000,
        "sourcePlatform": "LINKEDIN",
        "sourceUrl": "https://linkedin.com/jobs/view/123456",
        "postedAt": "2024-01-14T08:00:00Z",
        "analysis": {
          "requiredSkills": ["Java", "Spring Boot", "Microservices", "MySQL"],
          "niceToHaveSkills": ["Kafka", "Kubernetes"],
          "experienceMin": 3,
          "experienceMax": 6,
          "seniorityLevel": "SENIOR",
          "remoteType": "HYBRID",
          "domain": "FinTech"
        }
      },
      "createdAt": "2024-01-15T06:30:00Z"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 10,
    "totalElements": 47,
    "totalPages": 5
  }
}
```

---

## 5. Tailoring Endpoints

### POST /tailor/{jobId}

**Response 202:**
```json
{
  "message": "Tailoring initiated",
  "jobId": 42,
  "estimatedCompletionSeconds": 120
}
```

---

### GET /tailor/{id}

**Response 200:**
```json
{
  "id": 8,
  "userId": 1,
  "jobId": 42,
  "resumeId": 5,
  "atsScore": 91.5,
  "pdfReady": true,
  "modificationsLog": [
    {
      "field": "skills",
      "original": "Java, Spring Boot, REST APIs",
      "modified": "Java, Spring Boot, Microservices, REST APIs, Kafka",
      "reason": "Added Kafka and Microservices from JD required skills"
    },
    {
      "field": "experience[0].description",
      "original": "Built APIs for internal tools",
      "modified": "Designed and built RESTful microservices for internal tools, reducing latency by 40%",
      "reason": "Quantified impact; added microservices keyword from JD"
    }
  ],
  "tailoredContent": "...",
  "createdAt": "2024-01-15T06:32:00Z"
}
```

---

## 6. Application Tracking Endpoints

### GET /applications?status=INTERVIEW&page=0&size=20

**Response 200:**
```json
{
  "content": [
    {
      "id": 15,
      "userId": 1,
      "job": {
        "id": 42,
        "title": "Senior Java Developer",
        "company": "Infosys",
        "location": "Bangalore"
      },
      "status": "INTERVIEW",
      "appliedAt": "2024-01-15T09:00:00Z",
      "interviewDate": "2024-01-20T10:00:00Z",
      "notes": "Spoke with HR - technical round scheduled",
      "matchScore": 94.5,
      "createdAt": "2024-01-15T06:30:00Z"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

---

### PUT /applications/{id}

**Request:**
```json
{
  "status": "INTERVIEW",
  "notes": "Received call from Infosys HR. Technical round on Jan 20.",
  "interviewDate": "2024-01-20T10:00:00Z"
}
```

**Response 200:**
```json
{
  "id": 15,
  "status": "INTERVIEW",
  "notes": "Received call from Infosys HR. Technical round on Jan 20.",
  "interviewDate": "2024-01-20T10:00:00Z",
  "updatedAt": "2024-01-15T14:00:00Z"
}
```

---

## 7. Notification Endpoints

### GET /notifications?page=0&size=10

**Response 200:**
```json
{
  "content": [
    {
      "id": 201,
      "notificationType": "MATCH_ALERT",
      "subject": "🎯 New high-match job: Senior Java Developer at Infosys (94%)",
      "sentAt": "2024-01-15T06:35:00Z",
      "isRead": false,
      "metadata": {
        "matchId": 101,
        "jobId": 42,
        "score": 94.5
      }
    },
    {
      "id": 200,
      "notificationType": "DAILY_DIGEST",
      "subject": "☀️ Your Daily Job Digest - 5 new matches",
      "sentAt": "2024-01-15T07:00:00Z",
      "isRead": true,
      "readAt": "2024-01-15T08:30:00Z"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 10,
    "totalElements": 47,
    "totalPages": 5
  }
}
```

---

## 8. Error Response Reference

All error responses follow this schema:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_FAILED",
  "message": "Validation failed for 2 field(s)",
  "errors": [
    {"field": "email", "message": "must be a valid email address"},
    {"field": "password", "message": "must be at least 8 characters"}
  ],
  "path": "/api/v1/auth/register",
  "traceId": "abc-123-def-456"
}
```

---

*Document End — API Contracts v1.0*
