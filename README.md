# University Procurement Management System (UPMS) - Backend

**Status:** Under Active Development 🚀

A comprehensive microservices-based procurement management system designed for universities, compliant with Government Procurement Guidelines (Sri Lanka).

## Quick Start

### Using Docker Compose (Recommended)

```bash
# Start all services and databases
docker-compose up -d

# Verify services are running
docker ps

# Access Swagger Documentation
# - Auth Service: http://localhost:8081/swagger-ui.html
# - Procurement Service: http://localhost:8082/swagger-ui.html
# - Bidding Service: http://localhost:8083/swagger-ui.html
# - Evaluation Service: http://localhost:8084/swagger-ui.html

# Access PgAdmin (Database Management)
# - URL: http://localhost:5050
# - Email: admin@upms.local
# - Password: admin
```

## System Architecture

This project implements a scalable microservices architecture with four independent services:

1. **Auth Service** (Port 8081) - Authentication, Authorization, JWT, Azure AD SSO
2. **Procurement Service** (Port 8082) - RFQ creation, method selection, tender management
3. **Bidding Service** (Port 8083) - Bid submission, validation, bond & fee management
4. **Evaluation Service** (Port 8084) - Bid evaluation, approval routing, purchase orders

## Technology Stack

- **Java 17** + **Spring Boot 4.1.0**
- **PostgreSQL 15** with Flyway migrations
- **REST API** with OpenAPI/Swagger
- **JWT Authentication** + Azure AD/Outlook SSO
- **Docker & Docker Compose** for containerization
- **Maven** for build management

## Features

✅ Multi-stage bid evaluation (Preliminary → Technical → Financial)
✅ Automatic procurement method selection (NSM/NCB)
✅ Mandatory bid validation (bonds, fees, VAT/SSCL)
✅ Value-based approval routing
✅ Purchase order and contract management
✅ Goods receipt and quality assurance
✅ Payment SLA tracking
✅ Role-based access control (13 roles)
✅ Complete audit logging
✅ Azure AD/Outlook SSO integration

## Getting Started

See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for comprehensive documentation.

**Quick command:**
```bash
docker-compose up -d
```

## Documentation

- [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Complete API reference and setup guide
- Project documents in [Docs/](./Docs/) folder