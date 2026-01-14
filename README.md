# Smart URL Shortener

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-24.0-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A production-ready URL shortener microservice with analytics, built with Spring Boot. Features comprehensive monitoring, automated testing, and containerized deployment.

## Features

- ✅ **URL Shortening** - Convert long URLs into short, shareable links
- ✅ **Click Analytics** - Track clicks with timestamps and metadata
- ✅ **Health Monitoring** - Built-in health checks and metrics
- ✅ **RESTful API** - Clean JSON API with comprehensive documentation
- ✅ **Production Ready** - Includes logging, metrics, and distributed tracing
- ✅ **Docker Support** - Containerized with multi-stage builds
- ✅ **Kubernetes Ready** - Full K8s manifests with auto-scaling

## Table of Contents

- [Quick Start](#quick-start)
- [Setup Instructions](#setup-instructions)
- [Running Locally](#running-locally)
- [Docker Usage](#docker-usage)
- [API Documentation](#api-documentation)
- [Monitoring & Observability](#monitoring--observability)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Development](#development)

## Quick Start

The fastest way to get started:

```bash
# Clone the repository
git clone https://github.com/Iheb-Zenkri/url-shortener.git
cd url-shortener

# Run with Docker Compose (includes monitoring stack)
docker-compose up -d

# Application is now running at http://localhost:8080
```

Test it out:
```bash
# Shorten a URL
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.google.com"}'

# Response: {"shortCode":"abc123","shortUrl":"http://localhost:8080/abc123",...}

# Visit the short URL
curl -L http://localhost:8080/abc123
```

## Setup Instructions

### Prerequisites

Choose one of these setups based on how you want to run the application:

**Option 1: Local Development**
- Java 17 or higher ([Download](https://adoptium.net/))
- Maven 3.8+ ([Download](https://maven.apache.org/download.cgi))

**Option 2: Docker (Recommended)**
- Docker 24.0+ ([Download](https://www.docker.com/get-started))
- Docker Compose (included with Docker Desktop)

**Option 3: Kubernetes**
- Minikube or Kind ([Minikube Setup](https://minikube.sigs.k8s.io/docs/start/))
- kubectl ([Install Guide](https://kubernetes.io/docs/tasks/tools/))

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Iheb-Zenkri/url-shortener.git
   cd url-shortener
   ```

2. **Choose your runtime** (see sections below)

## Running Locally

Perfect for development and testing without Docker.

### Build and Run

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/url-shortener-1.0.0.jar
```

Or use Maven directly:
```bash
mvn spring-boot:run
```

### Configuration

The application uses `application.yml` for configuration. Default settings:

```yaml
server:
  port: 8080

logging:
  level:
    com.devops.urlshortener: INFO
```

### Verify Installation

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## Docker Usage

Docker provides consistent environments and includes the full monitoring stack.

### Build Docker Image

```bash
# Build the image
docker build -t url-shortener:latest .

# Run a single container
docker run -p 8080:8080 url-shortener:latest
```

### Using Docker Compose (Recommended)

Docker Compose launches the application plus monitoring tools (Prometheus, Grafana, Zipkin).

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f url-shortener

# Stop all services
docker-compose down
```

### Services Available

| Service | URL | Credentials |
|---------|-----|-------------|
| URL Shortener | http://localhost:8080 | - |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin/admin |
| Zipkin | http://localhost:9411 | - |

### Docker Commands Reference

```bash
# Build without cache
docker-compose build --no-cache

# View container status
docker-compose ps

# Stop and remove containers
docker-compose down -v

# Restart a specific service
docker-compose restart url-shortener

# View resource usage
docker stats
```


**Benefits:**
- 60% smaller final image size
- No build tools in production image
- Better security (minimal attack surface)

## API Documentation

### Base URL

- **Local:** `http://localhost:8080`
- **Docker:** `http://localhost:8080`
- **Kubernetes:** `http://<MINIKUBE_IP>:30080`

### Endpoints

#### 1. Shorten URL

Create a short URL from a long URL.

**Request:**
```http
POST /api/shorten
Content-Type: application/json

{
  "url": "https://www.example.com/very/long/url/path"
}
```

**Response:** (201 Created)
```json
{
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://www.example.com/very/long/url/path",
  "createdAt": "2025-01-15T10:30:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.github.com"}'
```

---

#### 2. Redirect to Original URL

Access a shortened URL and get redirected to the original.

**Request:**
```http
GET /{shortCode}
```

**Response:** 302 Redirect

**cURL Examples:**
```bash
# Follow redirect automatically
curl -L http://localhost:8080/abc123

# See redirect header (don't follow)
curl -I http://localhost:8080/abc123
```

---

#### 3. Get URL Statistics

View analytics for a shortened URL.

**Request:**
```http
GET /api/stats/{shortCode}
```

**Response:** (200 OK)
```json
{
  "shortCode": "abc123",
  "originalUrl": "https://www.github.com",
  "clickCount": 15,
  "createdAt": "2025-01-15T10:30:00",
  "lastAccessed": "2025-01-15T14:22:00",
  "clickHistory": [
    {
      "timestamp": "2025-01-15T10:35:00",
      "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...",
      "ipAddress": "192.168.1.1"
    }
  ]
}
```

**cURL Example:**
```bash
curl http://localhost:8080/api/stats/abc123
```

---

#### 4. Health Check

Check application health status.

**Request:**
```http
GET /actuator/health
```

**Response:** (200 OK)
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

**cURL Example:**
```bash
curl http://localhost:8080/actuator/health
```

---

#### 5. Metrics (Prometheus Format)

Get application metrics for monitoring.

**Request:**
```http
GET /actuator/prometheus
```

**Response:**
```
# HELP url_created_total Number of URLs shortened
# TYPE url_created_total counter
url_created_total 42.0

# HELP url_accessed_total Number of short URL accesses
# TYPE url_accessed_total counter
url_accessed_total 128.0
```

**cURL Example:**
```bash
curl http://localhost:8080/actuator/prometheus
```

### Error Responses

**404 Not Found** - Short code doesn't exist:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Short URL not found: xyz789"
}
```

**400 Bad Request** - Invalid URL:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid URL format"
}
```

## Monitoring & Observability

The application includes comprehensive monitoring capabilities.

### Metrics with Prometheus

**Access:** http://localhost:9090

**Query Examples:**
```
# Total URLs created
url_created_total

# Request rate (per second)
rate(http_server_requests_seconds_count[1m])

# 95th percentile latency
histogram_quantile(0.95, http_server_requests_seconds_bucket)
```

### Dashboards with Grafana

**Access:** http://localhost:3000 (admin/admin)

Pre-configured dashboards show:
- Request rate and latency
- Error rates
- JVM memory and CPU usage
- Custom business metrics

### Distributed Tracing with Zipkin

**Access:** http://localhost:9411

Trace requests across the application to identify bottlenecks and debug issues.

### Logs

Application logs are output in JSON format for easy parsing:

```bash
# View logs (Docker Compose)
docker-compose logs -f url-shortener

# Search logs for errors
docker-compose logs url-shortener | grep ERROR

# Follow logs for specific short code
docker-compose logs -f url-shortener | grep "abc123"
```

## Kubernetes Deployment

Deploy to a production-ready Kubernetes cluster.

### Prerequisites

```bash
# Start Minikube
minikube start --cpus=4 --memory=4096

# Enable addons
minikube addons enable ingress
minikube addons enable metrics-server
```

### Deploy Application

```bash
# Build image in Minikube's Docker environment
eval $(minikube docker-env)
docker build -t url-shortener:latest .

# Deploy all resources
kubectl apply -f k8s/

# Verify deployment
kubectl get all -n url-shortener

# Check pod status
kubectl get pods -n url-shortener -w
```

### Access Application

```bash
# Get Minikube IP
minikube ip
# Example output: 192.168.49.2

# Access via NodePort
curl http://192.168.49.2:30080/actuator/health

# Or use port forwarding
kubectl port-forward -n url-shortener service/url-shortener 8080:8080

# Now access at localhost
curl http://localhost:8080/actuator/health
```

### Kubernetes Features

- **High Availability:** 2 replicas by default
- **Auto-scaling:** HPA scales from 2-5 pods based on CPU/memory
- **Self-healing:** Automatic pod restart on failure
- **Rolling updates:** Zero-downtime deployments
- **Health checks:** Liveness and readiness probes

### Useful Commands

```bash
# View logs
kubectl logs -f -n url-shortener -l app=url-shortener

# Describe pod
kubectl describe pod -n url-shortener <pod-name>

# Scale manually
kubectl scale deployment url-shortener -n url-shortener --replicas=3

# Delete all resources
kubectl delete namespace url-shortener
```

## Development

### Project Structure

```
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/com/devops/urlshortener/
│   │   │   ├── UrlShortenerApplication.java
│   │   │   ├── controller/UrlController.java
│   │   │   ├── service/UrlService.java
│   │   │   └── model/UrlMapping.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── logback-spring.xml
│   └── test/
│       └── java/com/devops/urlshortener/
│           ├── UrlControllerTest.java
│           └── UrlServiceTest.java
├── k8s/                    # Kubernetes manifests
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

### Technology Stack

- **Framework:** Spring Boot 3.2
- **Language:** Java 17
- **Build Tool:** Maven
- **Metrics:** Micrometer + Prometheus
- **Tracing:** Zipkin
- **Testing:** JUnit 5, MockMvc

### Adding New Features

1. Create a feature branch
   ```bash
   git checkout -b feature/my-new-feature
   ```

2. Make your changes and test
   ```bash
   mvn test
   ```

3. Build and verify
   ```bash
   mvn clean package
   java -jar target/url-shortener-1.0.0.jar
   ```

4. Commit and push
   ```bash
   git add .
   git commit -m "feat: add new feature"
   git push origin feature/my-new-feature
   ```

### Running in Different Profiles

```bash
# Development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Docker profile
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Troubleshooting

### Common Issues

**Port 8080 already in use:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

**Docker build fails:**
```bash
# Clean Docker cache
docker system prune -a

# Rebuild without cache
docker-compose build --no-cache
```

**Kubernetes pod not starting:**
```bash
# Check pod logs
kubectl logs -n url-shortener <pod-name>

# Describe pod for events
kubectl describe pod -n url-shortener <pod-name>

# Check if image is available
kubectl get pods -n url-shortener -o jsonpath='{.items[*].spec.containers[*].image}'
```

**Tests failing:**
```bash
# Clean and rebuild
mvn clean install

# Run specific test
mvn test -Dtest=UrlControllerTest
```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For issues and questions:
- Create an issue on GitHub
- Check existing documentation in `/docs`
- Review the [API documentation](#api-documentation)

---

**Built with ❤️ as part of DevOps course project**