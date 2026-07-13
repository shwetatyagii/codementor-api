# CodeMentor AI API

An AI-powered Java code evaluation and interview preparation system built with Spring Boot.

## What It Does

Submit Java code → Get detailed AI analysis including:
- Code quality review
- Time & Space complexity (Big O)
- Optimization suggestions
- Java best practices
- Alternative approaches
- Common mistakes
- Interview questions

## Tech Stack

**Backend**
- Java 17 + Spring Boot 3.2.5
- Spring Security + JWT Authentication
- Spring Data JPA + Hibernate
- MySQL 8
- Groq AI API (Llama 3.3 70B)
- Swagger / OpenAPI 3

## Project Architecture
Controller Layer  →  validates input, delegates to service
Service Layer     →  business logic, AI orchestration
Repository Layer  →  JPA queries, data access
Security Layer    →  JWT filter, BCrypt hashing
AI Layer          →  AiService interface → GeminiAiService (Groq)

## API Endpoints

### Auth (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/signup | Register new user |
| POST | /api/auth/login | Login, get JWT token |

### Analysis (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/analysis/submit | Submit Java code for AI analysis |
| GET | /api/analysis/{id} | Get analysis by submission ID |

### Submissions (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/submissions | Get all submissions |
| GET | /api/submissions/{id} | Get submission with full analysis |
| DELETE | /api/submissions/{id} | Delete submission |
| GET | /api/submissions/search?q= | Search by title |

### Users (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/users/profile | Get user profile |
| PUT | /api/users/profile | Update full name |
| GET | /api/users/dashboard | Dashboard summary |

## Setup Instructions

### Prerequisites
- Java 17+
- MySQL 8
- Maven 3.8+
- Groq API Key (free at console.groq.com)

### 1. Clone the repository
```bash
git clone https://github.com/shwetatyagii/codementor-api.git
cd codementor-api
```

### 2. Database setup
```sql
CREATE DATABASE codementor_db CHARACTER SET utf8mb4;
CREATE USER 'codementor_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON codementor_db.* TO 'codementor_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure application.properties
```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```
Fill in your DB credentials and Groq API key.

### 4. Run
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

### 5. Access
- API: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html

## Security
- Passwords hashed with BCrypt
- JWT tokens (24hr expiry)
- All analysis/history endpoints require valid JWT
- API keys stored in application.properties (not committed to Git)

## Database Schema
users (id, username, email, password, full_name, role, created_at)
│
└── submissions (id, user_id, code_snippet, title, language, status, submitted_at)
│
└── analysis_results (id, submission_id, code_quality_review,
time_complexity, space_complexity,
optimization_tips, best_practices,
alternative_approach, common_mistakes,
interview_questions, raw_ai_response)

## Backend Repository

https://github.com/shwetatyagii/codementor-api

## Frontend Repository

https://github.com/shwetatyagii/codementor-ui
