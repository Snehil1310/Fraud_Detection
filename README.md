# Enterprise Fraud Detection System

![Java 17](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-brightgreen)
![Spring Batch](https://img.shields.io/badge/Spring_Batch-5.x-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![License](https://img.shields.io/badge/License-MIT-green)

An enterprise-grade, ML-powered fraud detection microservice designed to process massive datasets, identify anomalous transactions, and expose real-time analysis via a robust REST API.

Built to process the [Kaggle Credit Card Fraud Detection dataset](https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud) (284,807 transactions, 31 PCA-transformed columns), this project demonstrates scalable backend engineering, data pipelines, ML model integration, and cloud-native practices.

---

## 🏗️ Architecture

```ascii
┌──────────────────────────────────────────────────────────────────────────────┐
│                            EXTERNAL CLIENTS                                  │
│   (Payment Gateways, Back-office Dashboards, Mobile Banking Apps)            │
└───────┬───────────────────────────────┬──────────────────────────────┬───────┘
        │ POST /analyze                 │ GET /alerts                  │
        ▼                               ▼                              │
┌──────────────────────────────────────────────────────────────────────┼───────┐
│                           API GATEWAY / WAF                          │       │
│                (Rate Limiting: 20 req/min per IP)                    │       │
└───────┬───────────────────────────────┬──────────────────────────────┼───────┘
        │                               │                              │ GET /stats
        ▼                               ▼                              ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT MICROSERVICE                                  │
│                                                                              │
│  ┌──────────────────┐   ┌──────────────────────┐   ┌──────────────────────┐  │
│  │                  │   │                      │   │                      │  │
│  │  REST Controller ├───▶   Scoring Service    ├───▶  Batch Controller    │  │
│  │  (Swagger UI)    │   │  (Feature Eng.)      │   │  (Spring Batch)      │  │
│  └────────┬─────────┘   └──────────┬───────────┘   └──────────┬───────────┘  │
│           │                        │                          │              │
│           │                        ▼                          │              │
│           │             ┌──────────────────────┐              │              │
│           │             │  Strategy Context    │              │              │
│           │             │ (Runtime Switching)  │              │              │
│           │             └────┬─────────────┬───┘              │              │
│           │                  │             │                  │              │
│           ▼                  ▼             ▼                  ▼              │
│  ┌──────────────────┐   ┌─────────┐   ┌─────────┐  ┌──────────────────────┐  │
│  │                  │   │ Random  │   │  Rule   │  │   FlatFileItemReader │  │
│  │   JPA / Hibernate│◀──┤ Forest  │   │  Based  │  │   (Chunk Size: 1000) │  │
│  │                  │   │ (Weka)  │   │ Engine  │  │                      │  │
│  └────────┬─────────┘   └─────────┘   └─────────┘  └──────────────────────┘  │
└───────────┼──────────────────────────────────────────────────────────────────┘
            │
            ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                             DATA STORE                                       │
│                         (MySQL 8.0 - Dockerized)                             │
└──────────────────────────────────────────────────────────────────────────────┘
```

See [ARCHITECTURE.md](docs/ARCHITECTURE.md) for how this batch-oriented pipeline translates into a real-time streaming pipeline using Apache Kafka and Spark in a true Big Data ecosystem.

---

## ✨ Key Features

- **Enterprise Data Pipeline**: `Spring Batch` implementation with memory-safe chunk-oriented processing for ingesting 284K+ rows of raw transaction data. Includes feature engineering (Z-score normalization, time-based anomaly detection).
- **Machine Learning Integration**: Implements the **Strategy Design Pattern** to seamlessly switch between a `Weka Random Forest` classifier and a fallback rule-based heuristic engine with zero downtime.
- **Robust REST API**: Documented with `Swagger/OpenAPI 3.0`. Includes global exception handling (`@ControllerAdvice`) for graceful error recovery and standardized `ApiError` responses.
- **Security First**: Defends against DoS attacks with `Bucket4j` per-IP rate limiting, blocks XSS payload injections via custom servlet filters, and prevents SQL injection using JPA parameterized queries. See [SECURITY.md](SECURITY.md).
- **Cloud Native CI/CD**: Fully containerized using a multi-stage `Dockerfile`. Includes a complete `docker-compose` environment and a GitHub Actions pipeline (`ci.yml`) enforcing an 80% minimum test coverage gate via `JaCoCo`.

---

## 🚀 Quick Start (Local Setup)

### Prerequisites
- Docker & Docker Compose
- Java 17
- Maven

### 1. Download the Dataset
Download `creditcard.csv` from [Kaggle](https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud) and place it in the project:
`src/main/resources/data/creditcard.csv`

### 2. Run with Docker Compose
The easiest way to start the system (includes both the Spring Boot app and MySQL 8.0):

```bash
docker-compose up -d --build
```

The application will be available at `http://localhost:8080`.

### 3. Trigger Data Ingestion
Trigger the Spring Batch job to ingest the CSV data:
```bash
curl -X POST http://localhost:8080/api/v1/batch/ingest
```
Watch the logs to see the chunk processing: `docker logs -f fraud-detection-app`

---

## 📖 API Documentation

Once the application is running, access the interactive Swagger UI:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Sample API Request

**Analyze a Transaction (`POST /api/v1/transactions/analyze`)**
```json
{
  "time": 406.0,
  "v1": -2.312,
  "v2": 1.951,
  "v3": -1.609,
  "v4": 3.997,
  "v5": -0.522,
  "v6": -1.426,
  "v7": -2.537,
  "v8": 1.391,
  "v9": -2.770,
  "v10": -2.772,
  "v11": 3.202,
  "v12": -2.899,
  "v13": -0.595,
  "v14": -4.289,
  "v15": 0.389,
  "v16": -1.140,
  "v17": -2.830,
  "v18": -0.016,
  "v19": 0.416,
  "v20": 0.126,
  "v21": 0.517,
  "v22": -0.035,
  "v23": -0.465,
  "v24": 0.320,
  "v25": 0.044,
  "v26": 0.177,
  "v27": 0.261,
  "v28": -0.143,
  "amount": 0.00
}
```

---

## 🧪 Testing

Run the comprehensive unit and integration test suite (JUnit 5 + Mockito). The build enforce a multi-module JaCoCo coverage minimum of 80%.

```bash
./mvnw clean verify
```

Coverage report available at: `target/site/jacoco/index.html`

---

## ☁️ Cloud Deployment

This project is built for containerized cloud deployment.
The GitHub Actions workflow automatically builds and pushes the Docker image to Docker Hub (`snehil1310/fraud-detection-system`) upon merging to `main`.

Recommended deployment targets:
- **AWS**: Elastic Container Service (ECS) with Fargate or Elastic Beanstalk
- **GCP**: Google Cloud Run
- **Azure**: Azure Container Apps
