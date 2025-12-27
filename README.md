# Smart URL Shortener with Analytics

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-24.0-blue.svg)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.28-326CE5.svg)](https://kubernetes.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A production-ready URL shortener microservice demonstrating modern DevOps practices, built as part of a Software Engineering DevOps course project. This application showcases end-to-end implementation of CI/CD pipelines, containerization, orchestration, observability, and security scanning.

---

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Architecture](#-architecture)
- [DevOps Concepts Implemented](#-devops-concepts-implemented)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Observability](#-observability)
- [Security](#-security)
- [Kubernetes Deployment](#-kubernetes-deployment)
- [Development Workflow](#-development-workflow)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Project Overview

### Academic Context

This project was developed as part of the **DevOps** course in the final year of Software Engineering studies. The primary objective is to demonstrate practical understanding and implementation of modern DevOps practices, including:

- **Continuous Integration/Continuous Deployment (CI/CD)**
- **Infrastructure as Code (IaC)**
- **Container Orchestration**
- **Observability and Monitoring**
- **Security-First Development**
- **Automated Testing**

### Business Context

The URL Shortener service provides:
- **URL Shortening**: Convert long URLs into short, manageable links
- **Analytics**: Track click counts, timestamps, and user agents
- **Redirect Service**: Seamless redirection to original URLs
- **Health Monitoring**: Real-time application health status
- **Metrics Export**: Prometheus-compatible metrics endpoint

### Key Features

- ✅ RESTful API with JSON responses
- ✅ In-memory storage with thread-safe operations
- ✅ Click analytics with detailed metadata
- ✅ Rate limiting for abuse prevention
- ✅ Comprehensive observability (metrics, logs, traces)
- ✅ Production-ready health checks
- ✅ Automated security scanning (SAST/DAST)
- ✅ Horizontal pod autoscaling
- ✅ Multi-stage Docker builds
- ✅ Kubernetes-native deployment

---

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│                   (HTTP/REST Clients)                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Ingress Controller                       │
│                  (NGINX - url-shortener.local)              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Service                        │
│              (ClusterIP + NodePort: 30080)                  │
└─────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
         ┌──────────────────┐  ┌──────────────────┐
         │   Pod 1          │  │   Pod 2          │
         │  (Replica 1)     │  │  (Replica 2)     │
         │                  │  │                  │
         │  Spring Boot     │  │  Spring Boot     │
         │  Application     │  │  Application     │
         │  Port: 8080      │  │  Port: 8080      │
         └──────────────────┘  └──────────────────┘
                    │                   │
                    └─────────┬─────────┘
                              ▼
         ┌──────────────────────────────────────┐
         │     Observability Stack              │
         │                                      │
         │  ┌──────────┐  ┌──────────┐        │
         │  │Prometheus│  │  Zipkin  │        │
         │  │  :9090   │  │  :9411   │        │
         │  └──────────┘  └──────────┘        │
         │                                      │
         │  ┌──────────┐                       │
         │  │ Grafana  │                       │
         │  │  :3000   │                       │
         │  └──────────┘                       │
         └──────────────────────────────────────┘
```

### Application Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           UrlController (REST API)                   │  │
│  │  • POST /api/shorten                                 │  │
│  │  • GET /{shortCode}                                  │  │
│  │  • GET /api/stats/{shortCode}                        │  │
│  │  • GET /actuator/health                              │  │
│  │  • GET /actuator/prometheus                          │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Business Logic Layer                     │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              UrlService                              │  │
│  │  • shortenUrl()                                      │  │
│  │  • getOriginalUrl()                                  │  │
│  │  • recordClick()                                     │  │
│  │  • getStats()                                        │  │
│  │  • generateShortCode()                               │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │     ConcurrentHashMap (In-Memory Storage)            │  │
│  │  • Thread-safe operations                            │  │
│  │  • O(1) lookup complexity                            │  │
│  │  • Ephemeral storage                                 │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Cross-Cutting Concerns                      │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Logging     │  │   Metrics    │  │   Tracing    │     │
│  │  (Logback)   │  │(Micrometer)  │  │  (Zipkin)    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 DevOps Concepts Implemented

### 1. Version Control & Collaboration

**Concept**: Git-based workflow with structured branching and code review processes.

**Implementation**:
- **Git/GitHub**: Distributed version control system
- **Branch Strategy**: Feature branches with protected main branch
- **Pull Requests**: Mandatory code reviews before merging
- **GitHub Issues**: Task tracking and project management
- **GitHub Projects**: Kanban board for sprint planning

**Academic Relevance**: Demonstrates collaborative software development practices essential in professional environments, including peer review and change management.

```bash
# Example workflow
git checkout -b feature/2-implement-shorten-endpoint
# Make changes
git commit -m "feat: implement URL shortening endpoint"
git push origin feature/2-implement-shorten-endpoint
# Create PR for review
```

---

### 2. Continuous Integration (CI)

**Concept**: Automated building and testing of code changes to detect integration issues early.

**Implementation**:
- **GitHub Actions**: Cloud-based CI/CD platform
- **Automated Builds**: Maven compilation on every push/PR
- **Unit Testing**: JUnit 5 test execution with coverage reports
- **Code Quality**: SpotBugs static analysis
- **Dependency Scanning**: Automated vulnerability detection

**Academic Relevance**: Reduces integration risks and ensures code quality through automation, a cornerstone of modern software engineering.

**Pipeline Stages**:
```yaml
1. Checkout Code
2. Setup Java 17
3. Build with Maven
4. Run Unit Tests
5. Generate Coverage Reports
6. Upload Test Artifacts
```

---

### 3. Security Scanning

**Concept**: Automated security testing at multiple stages of the development lifecycle.

**Implementation**:

#### a) SAST (Static Application Security Testing)
- **Tool**: SpotBugs, Dependency-Check
- **Scope**: Source code analysis before deployment
- **Detects**: Code vulnerabilities, insecure patterns, dependency vulnerabilities

#### b) DAST (Dynamic Application Security Testing)
- **Tool**: OWASP ZAP (Zed Attack Proxy)
- **Scope**: Runtime analysis of deployed application
- **Detects**: XSS, SQL injection, security misconfigurations

**Academic Relevance**: Implements "Security as Code" principle, shifting security left in the development lifecycle rather than treating it as an afterthought.

**Security Pipeline**:
```
Code Commit → SAST Scan → Build → Deploy → DAST Scan → Production
```

---

### 4. Containerization

**Concept**: Package applications with their dependencies into portable, isolated containers.

**Implementation**:
- **Docker**: Container runtime and image builder
- **Multi-Stage Builds**: Optimized image size (Build stage + Runtime stage)
- **Base Images**: Alpine Linux for minimal footprint
- **Security**: Non-root user execution
- **Health Checks**: Built-in container health monitoring

**Academic Relevance**: Solves the "works on my machine" problem by ensuring consistency across development, testing, and production environments.

**Dockerfile Structure**:
```dockerfile
# Stage 1: Build (Maven + JDK)
FROM maven:3.9-eclipse-temurin-17-alpine AS build
# Build application

# Stage 2: Runtime (JRE only)
FROM eclipse-temurin:17-jre-alpine
# Copy JAR and run
```

**Benefits**:
- **Portability**: Run anywhere Docker is supported
- **Isolation**: Dependencies don't conflict with host system
- **Efficiency**: 60% smaller image size with multi-stage builds
- **Reproducibility**: Same image produces identical containers

---

### 5. Container Orchestration

**Concept**: Automated deployment, scaling, and management of containerized applications.

**Implementation**:
- **Kubernetes**: Industry-standard container orchestration platform
- **Deployments**: Declarative application deployment with rolling updates
- **Services**: Load balancing and service discovery
- **ConfigMaps**: External configuration management
- **Horizontal Pod Autoscaler (HPA)**: Automatic scaling based on metrics
- **Ingress**: HTTP routing and load balancing

**Academic Relevance**: Demonstrates cloud-native architecture and infrastructure automation, critical skills for modern software engineers.

**Kubernetes Components**:

| Component | Purpose | Configuration |
|-----------|---------|---------------|
| Deployment | Manages pods and replica sets | 2 replicas, rolling updates |
| Service | Network access to pods | ClusterIP + NodePort |
| ConfigMap | Application configuration | Externalized settings |
| HPA | Auto-scaling | 2-5 replicas, CPU/Memory based |
| Ingress | External HTTP access | Domain-based routing |

**Self-Healing Capabilities**:
- Automatic pod restart on failure
- Health check monitoring (liveness/readiness probes)
- Rollback on deployment failures
- Node failure tolerance

---

### 6. Observability (The Three Pillars)

**Concept**: Comprehensive monitoring and debugging of distributed systems through metrics, logs, and traces.

#### a) Metrics (Quantitative Data)

**Implementation**: Micrometer + Prometheus + Grafana

**Purpose**: Numerical measurements of system behavior over time

**Metrics Collected**:
- **Application Metrics**:
    - `url_created_total`: Counter of shortened URLs
    - `url_accessed_total`: Counter of URL accesses
    - `http_server_requests_seconds`: Request latency histogram
- **JVM Metrics**:
    - `jvm_memory_used_bytes`: Memory consumption
    - `jvm_threads_live`: Thread count
    - `jvm_gc_pause_seconds`: Garbage collection pauses
- **System Metrics**:
    - CPU usage
    - Memory utilization
    - Disk I/O

**Visualization**: Grafana dashboards with real-time graphs

#### b) Logs (Contextual Information)

**Implementation**: Logback + JSON encoding

**Purpose**: Detailed event records for debugging and auditing

**Log Structure** (JSON):
```json
{
  "@timestamp": "2025-01-15T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.devops.urlshortener.controller.UrlController",
  "message": "Creating short URL for: https://example.com",
  "traceId": "abc123",
  "spanId": "def456"
}
```

**Log Levels**:
- **DEBUG**: Detailed diagnostic information
- **INFO**: General informational messages
- **WARN**: Potential issues that don't stop execution
- **ERROR**: Error events that might still allow app to continue

#### c) Traces (Request Flow)

**Implementation**: Micrometer Tracing + Zipkin

**Purpose**: Track requests across distributed services

**Trace Information**:
- Request path through services
- Time spent in each service
- Service dependencies
- Error propagation

**Academic Relevance**: Observability is essential for operating production systems. The three pillars provide complementary views: metrics for trends, logs for details, traces for relationships.

---

### 7. Infrastructure as Code (IaC)

**Concept**: Managing infrastructure through declarative configuration files rather than manual processes.

**Implementation**:
- **Kubernetes Manifests**: YAML files defining cluster state
- **Docker Compose**: Multi-container application definition
- **GitHub Actions Workflows**: CI/CD pipeline as code

**Academic Relevance**: IaC enables version control of infrastructure, reproducible deployments, and disaster recovery through code.

**Benefits**:
- **Version Control**: Infrastructure changes tracked in Git
- **Reproducibility**: Identical environments from same code
- **Documentation**: Infrastructure is self-documenting
- **Automation**: No manual configuration drift

**Example** (Kubernetes Deployment):
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: url-shortener
spec:
  replicas: 2
  # ... declarative specification
```

---

### 8. Continuous Deployment (CD)

**Concept**: Automated deployment of validated changes to production environments.

**Implementation**:
- **Docker Hub**: Container registry for image storage
- **Kubernetes Rolling Updates**: Zero-downtime deployments
- **Automated Rollbacks**: Revert to previous version on failure
- **Environment Promotion**: Dev → Staging → Production

**Academic Relevance**: CD reduces time-to-market and deployment risks through automation and standardization.

**Deployment Process**:
```
1. Code Merged to Main
2. CI Pipeline Runs (Build, Test, Scan)
3. Docker Image Built and Pushed
4. Kubernetes Deployment Updated
5. Rolling Update Performed
6. Health Checks Validated
7. Deployment Complete
```

---

### 9. Configuration Management

**Concept**: Separate configuration from code to enable environment-specific settings without code changes.

**Implementation**:
- **Spring Profiles**: Environment-specific configurations (dev, docker, kubernetes)
- **Kubernetes ConfigMaps**: External configuration injection
- **Environment Variables**: Runtime configuration
- **application.yml**: Hierarchical configuration structure

**Academic Relevance**: Follows the Twelve-Factor App methodology, enabling portability and maintainability.

**Configuration Hierarchy**:
```
application.yml (defaults)
  └─> application-docker.yml (Docker overrides)
      └─> Environment Variables (Runtime overrides)
          └─> ConfigMap (Kubernetes overrides)
```

---

### 10. Automated Testing

**Concept**: Comprehensive testing strategy with different test types at multiple levels.

**Implementation**:
- **Unit Tests**: JUnit 5 + MockMvc
- **Integration Tests**: Spring Boot Test
- **API Tests**: REST Assured
- **Load Tests**: Apache JMeter (optional)

**Test Pyramid**:
```
        ┌─────┐
        │  E2E │  (Few, slow, expensive)
        └─────┘
      ┌─────────┐
      │Integration│  (Some, medium speed)
      └─────────┘
    ┌─────────────┐
    │   Unit Tests  │  (Many, fast, cheap)
    └─────────────┘
```

**Academic Relevance**: Automated testing ensures code correctness, prevents regressions, and enables confident refactoring.

---

## 🛠️ Technology Stack

### Backend Framework
- **Spring Boot 3.2**: Modern Java framework for building production-ready applications
- **Java 17**: Latest LTS version with improved performance and features
- **Maven**: Dependency management and build automation

### Observability
- **Micrometer**: Application metrics facade
- **Prometheus**: Time-series metrics database
- **Grafana**: Metrics visualization and dashboards
- **Zipkin**: Distributed tracing system
- **Logback**: Logging framework with JSON encoding

### Containerization & Orchestration
- **Docker**: Container platform
- **Kubernetes**: Container orchestration
- **Minikube**: Local Kubernetes cluster for development

### CI/CD
- **GitHub Actions**: Automated workflows
- **Docker Hub**: Container registry

### Security
- **SpotBugs**: Static code analysis
- **Dependency-Check**: Vulnerability scanning
- **OWASP ZAP**: Dynamic security testing

### Testing
- **JUnit 5**: Unit testing framework
- **MockMvc**: Spring MVC testing
- **AssertJ**: Fluent assertions

---

## 📁 Project Structure

```
url-shortener/
├── .github/
│   └── workflows/
│       └── ci-cd.yml              # GitHub Actions CI/CD pipeline
├── src/
│   ├── main/
│   │   ├── java/com/devops/urlshortener/
│   │   │   ├── UrlShortenerApplication.java    # Main application class
│   │   │   ├── controller/
│   │   │   │   └── UrlController.java          # REST API endpoints
│   │   │   ├── service/
│   │   │   │   └── UrlService.java             # Business logic
│   │   │   ├── model/
│   │   │   │   └── UrlMapping.java             # Data model
│   │   │   └── config/
│   │   │       └── ObservabilityConfig.java    # Metrics configuration
│   │   └── resources/
│   │       ├── application.yml                  # Application configuration
│   │       └── logback-spring.xml              # Logging configuration
│   └── test/
│       └── java/com/devops/urlshortener/
│           ├── UrlControllerTest.java          # Controller tests
│           └── UrlServiceTest.java             # Service tests
├── k8s/
│   ├── namespace.yaml             # Kubernetes namespace
│   ├── configmap.yaml             # Configuration
│   ├── deployment.yaml            # Application deployment
│   ├── service.yaml               # Services (ClusterIP + NodePort)
│   ├── ingress.yaml               # Ingress controller
│   ├── hpa.yaml                   # Horizontal Pod Autoscaler
│   └── monitoring.yaml            # Prometheus & Zipkin
├── monitoring/
│   ├── prometheus.yml             # Prometheus configuration
│   └── grafana/
│       ├── datasources/
│       │   └── datasource.yml     # Grafana datasource
│       └── dashboards/
│           └── dashboard.json     # Custom dashboards
├── Dockerfile                     # Multi-stage Docker build
├── .dockerignore                  # Docker build exclusions
├── docker-compose.yml             # Local development stack
├── pom.xml                        # Maven dependencies
├── README.md                      # This file
└── REPORT.md                      # Final project report
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Docker 24.0+** ([Download](https://www.docker.com/get-started))
- **Docker Compose** (included with Docker Desktop)
- **Kubernetes** (Minikube or Kind) ([Minikube](https://minikube.sigs.k8s.io/docs/start/))
- **kubectl** ([Install](https://kubernetes.io/docs/tasks/tools/))

### Option 1: Run Locally (Development)

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/url-shortener.git
cd url-shortener

# Build the project
mvn clean package

# Run the application
java -jar target/url-shortener-1.0.0.jar

# Or using Maven
mvn spring-boot:run

# Application will be available at http://localhost:8080
```

### Option 2: Run with Docker Compose (Recommended)

```bash
# Start all services (app + observability stack)
docker-compose up -d --build

# Check running containers
docker-compose ps

# View logs
docker-compose logs -f url-shortener

# Stop all services
docker-compose down
```

**Services Available**:
- Application: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- Zipkin: http://localhost:9411

### Option 3: Deploy to Kubernetes

```bash
# Start minikube
minikube start --cpus=4 --memory=4096

# Enable required addons
minikube addons enable ingress
minikube addons enable metrics-server

# Build and load Docker image
eval $(minikube docker-env)
docker build -t url-shortener:latest .

# Deploy to Kubernetes
kubectl apply -f k8s/

# Check deployment status
kubectl get all

# Get minikube IP
minikube ip

# Access application
# http://<MINIKUBE_IP>:30080
```

---

## 📚 API Documentation

### Base URL
- **Local**: `http://localhost:8080`
- **Docker**: `http://localhost:8080`
- **Kubernetes**: `http://<MINIKUBE_IP>:30080`

### Endpoints

#### 1. Create Short URL

**POST** `/api/shorten`

**Request Body**:
```json
{
  "url": "https://www.example.com/very/long/url/path"
}
```

**Response** (201 Created):
```json
{
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://www.example.com/very/long/url/path",
  "createdAt": "2025-01-15T10:30:00"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.google.com"}'
```

---

#### 2. Redirect to Original URL

**GET** `/{shortCode}`

**Response**: 302 Redirect to original URL

**cURL Example**:
```bash
# Follow redirect
curl -L http://localhost:8080/abc123

# Don't follow redirect (see headers)
curl -I http://localhost:8080/abc123
```

---

#### 3. Get URL Statistics

**GET** `/api/stats/{shortCode}`

**Response** (200 OK):
```json
{
  "shortCode": "abc123",
  "originalUrl": "https://www.google.com",
  "clickCount": 15,
  "createdAt": "2025-01-15T10:30:00",
  "clickHistory": [
    {
      "timestamp": "2025-01-15T10:35:00",
      "userAgent": "Mozilla/5.0...",
      "ipAddress": "192.168.1.1"
    }
  ]
}
```

**cURL Example**:
```bash
curl http://localhost:8080/api/stats/abc123
```

---

#### 4. Health Check

**GET** `/actuator/health`

**Response** (200 OK):
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 400000000000,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

#### 5. Prometheus Metrics

**GET** `/actuator/prometheus`

**Response**: Prometheus-formatted metrics

```
# HELP url_created_total Number of URLs shortened
# TYPE url_created_total counter
url_created_total 42.0

# HELP url_accessed_total Number of short URL accesses
# TYPE url_accessed_total counter
url_accessed_total 128.0

# HELP http_server_requests_seconds HTTP request latency
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="POST",uri="/api/shorten",status="201"} 42.0
http_server_requests_seconds_sum{method="POST",uri="/api/shorten",status="201"} 0.523
```

---

## 🔄 CI/CD Pipeline

### Pipeline Overview

The CI/CD pipeline is implemented using **GitHub Actions** and consists of 6 jobs that run sequentially and in parallel for optimal efficiency.

### Pipeline Jobs

```
┌─────────────────────────────────────────────────────────┐
│                    Trigger (Push/PR)                     │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│             Job 1: Build and Test (5-7 min)             │
│  • Checkout code                                         │
│  • Setup Java 17                                         │
│  • Maven build                                           │
│  • Run unit tests                                        │
│  • Generate coverage reports                             │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│          Job 2: SAST Security Scan (3-5 min)            │
│  • SpotBugs static analysis                              │
│  • Dependency vulnerability check                        │
│  • Upload security reports                               │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│          Job 3: Build Docker Image (5-8 min)            │
│  • Build JAR file                                        │
│  • Build Docker image                                    │
│  • Push to Docker Hub                                    │
│  • Tag: latest, branch name, SHA                         │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│          Job 4: DAST Security Scan (5-10 min)           │
│  • Pull Docker image                                     │
│  • Run container                                         │
│  • OWASP ZAP baseline scan                               │
│  • Upload scan results                                   │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│       Job 5: Deploy to Kubernetes (2-3 min)             │
│  (Only on main branch)                                   │
│  • Update deployment image                               │
│  • Wait for rollout                                      │
│  • Verify deployment                                     │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│              Job 6: Notifications (1 min)                │
│  • Send success/failure notifications                    │
└─────────────────────────────────────────────────────────┘
```

### Pipeline Configuration

**File**: `.github/workflows/ci-cd.yml`

**Triggers**:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`

**Required Secrets**:
1. `DOCKERHUB_USERNAME` - Docker Hub username
2. `DOCKERHUB_TOKEN` - Docker Hub access token
3. `KUBECONFIG` - Base64-encoded Kubernetes config (for deployment)

### Setting Up CI/CD

```bash
# 1. Create GitHub secrets
# Go to: Settings → Secrets and variables → Actions

# 2. Add Docker Hub credentials
# DOCKERHUB_USERNAME: your-username
# DOCKERHUB_TOKEN: (generate at hub.docker.com/settings/security)

# 3. Add Kubernetes config (optional, for auto-deployment)
cat ~/.kube/config | base64 -w 0
# KUBECONFIG: <paste base64 output>

# 4. Push code to trigger pipeline
git push origin main
```

### Pipeline Artifacts

After each run, the following artifacts are available:

1. **test-results**: JUnit test reports
2. **coverage-report**: JaCoCo code coverage HTML report
3. **sast-results**: SpotBugs and dependency check reports
4. **dast-results**: OWASP ZAP security scan report

**Access**: GitHub Actions → Select run → Artifacts section

---

## 📊 Observability

### Metrics (Prometheus)

**Access**: http://localhost:9090 (Docker Compose) or http://<MINIKUBE_IP>:30090 (Kubernetes)

**Custom Application Metrics**:
- `url_created_total