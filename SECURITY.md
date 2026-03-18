# Security Policy

## Supported Versions

Currently, the `main` branch (version 1.0.x) is the only supported version receiving security updates.

## Data Handling & Protection

This Fraud Detection System processes highly sensitive financial data. As such, several core protections are engineered directly into the architecture:

### 1. Payment Data Anonymization
The core Kaggle dataset utilizes **PCA (Principal Component Analysis)** transformations. The input values `V1` through `V28` are anonymized dimensional embeddings. No Personally Identifiable Information (PII) such as names, raw credit card numbers, or addresses are stored, processed, or logged by this system. 

### 2. Rate Limiting (DDoS & Brute Force Prevention)
To protect ML computational resources and prevent API abuse, **Bucket4j** is implemented on all analysis endpoints (`/api/v1/transactions/analyze` and `/api/v1/transactions/score`). 
- **Limit:** 20 requests per minute per IP address.
- **Algorithm:** Token bucket algorithm.
- **Response:** HTTP 429 Too Many Requests when the threshold is exceeded.

### 3. Input Sanitization (XSS Prevention)
A custom servlet filter (`XssSanitizationFilter`) intercepts all incoming HTTP requests. 
- It wraps the `HttpServletRequest` to aggressively strip potential executable payloads (e.g., `<script>`, `eval()`, `javascript:`).
- HTML entity encoding is applied to mitigate Cross-Site Scripting (XSS) vectors.

### 4. SQL Injection Prevention
All database interactions use **Spring Data JPA/Hibernate**. 
- The system exclusively uses parameterized queries for both read and write operations.
- Direct string concatenation in SQL queries is strictly prohibited across the codebase.
- User inputs (such as ID lookups or pagination parameters) are strictly typed.

### 5. Jakarta Bean Validation
The `/analyze` endpoint relies strictly on `@Valid` DTOs (`TransactionAnalysisRequest`). 
- The system enforces strict numerical boundary checks (`@NotNull`, `@DecimalMin`, etc.) on all inputs before business logic is executed.
- Invalid requests immediately trigger an HTTP 400 Bad Request via the `@ControllerAdvice` global exception handler, preventing malformed data from reaching the ML scoring layer.

---

## Reporting a Vulnerability

If you discover a security vulnerability within this project, please do not disclose it publicly.
Instead, report the issue via email to the core maintainers:
**Security Contact:** fraud-detection@enterprise.com

Please include the following details in your report:
- Type of vulnerability (e.g., XSS, SQLi, Logic Flaw)
- Steps to reproduce
- Potential impact

We will evaluate the issue and aim to release a patch within 48 hours for critical vulnerabilities.
