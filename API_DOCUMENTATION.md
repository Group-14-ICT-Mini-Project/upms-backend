# UPMS Backend - University Procurement Management System

## System Architecture Overview

UPMS Backend is a microservices-based system built with Spring Boot 4.1.0, PostgreSQL, and follows industry best practices for security, scalability, and maintainability.

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        API Gateway / Load Balancer                  │
│                         (Future Enhancement)                        │
└────────────────────────────────────────────────────────────────────┬┘
                            │
                ┌───────────┼───────────┐
                │           │           │
        ┌───────▼────┐ ┌────▼──────┐ ┌─▼────────────┐
        │  Auth      │ │Procurement│ │ Bidding      │
        │  Service   │ │ Service   │ │ Service      │
        │  (8081)    │ │ (8082)    │ │ (8083)       │
        └───────┬────┘ └────┬──────┘ └─┬────────────┘
                │           │         │
        ┌───────▼────┐ ┌────▼──────┐ ┌▼────────────┐
        │   Auth     │ │Procurement│ │ Bidding     │
        │    DB      │ │    DB     │ │   DB        │
        │  PostgreSQL│ │PostgreSQL │ │PostgreSQL   │
        └────────────┘ └───────────┘ └─────────────┘
                            │
                        ┌───▼──────┐
                        │Evaluation │
                        │ Service   │
                        │ (8084)    │
                        └───┬──────┘
                            │
                        ┌───▼──────┐
                        │Evaluation │
                        │    DB     │
                        │PostgreSQL │
                        └───────────┘
```

## Microservices

### 1. **Auth Service** (Port: 8081)
**Responsibilities:**
- User authentication and authorization
- JWT token generation and validation
- Azure AD/Outlook SSO integration
- Role-based access control (RBAC)
- Audit logging
- Refresh token management

**Key Endpoints:**
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh-token` - Refresh JWT token
- `GET /api/v1/auth/verify` - Verify JWT token
- `POST /api/v1/auth/logout` - User logout
- `GET /api/v1/auth/health` - Service health

**Database:** `upms_auth`

---

### 2. **Procurement Service** (Port: 8082)
**Responsibilities:**
- Procurement creation and management
- Automatic procurement method selection (NSM vs NCB)
- RFQ generation and distribution
- Newspaper publication management
- Promise.lk posting
- Procurement status tracking

**Key Entities:**
- Procurements (RFQs, Tenders)
- Procurement Methods
- Procurement Categories
- RFQ Recipients
- Newspaper Publications
- Promise.lk Posts

**Database:** `upms_procurement`

---

### 3. **Bidding Service** (Port: 8083)
**Responsibilities:**
- Bid submission and management
- Bid bond validation
- Document fee tracking
- VAT/SSCL declaration verification
- Bid amendment handling
- Bid rejection appeals

**Key Entities:**
- Bids
- Bid Documents
- Bid Bonds
- Document Fees
- VAT/SSCL Declarations
- Bid Amendments
- Bid Rejections

**Database:** `upms_bidding`

---

### 4. **Evaluation Service** (Port: 8084)
**Responsibilities:**
- Preliminary bid examination
- Technical evaluation (TEC management)
- Financial evaluation and comparison
- Bid Evaluation Summary (BES) report generation
- Value-based approval routing
- Purchase order creation
- Performance bond tracking
- Goods receipt management
- Quality assurance
- Payment SLA tracking

**Key Entities:**
- Bid Evaluations
- Technical Evaluation Criteria & Scores
- Financial Evaluation Results
- TEC Members
- BES Reports
- Approval Routes
- Purchase Orders
- Performance Bonds
- Goods Received Notes
- Quality Reports
- Payment Tracking

**Database:** `upms_evaluation`

---

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 4.1.0
- **Web:** Spring MVC, REST API
- **Security:** Spring Security, JWT (JJWT), OAuth2, Azure AD
- **Database:** PostgreSQL 15
- **ORM:** Spring Data JPA, Hibernate
- **Migration:** Flyway
- **API Documentation:** OpenAPI 3.0 / Swagger
- **Logging:** SLF4J + Logback
- **Build Tool:** Maven 3.9+
- **Containerization:** Docker & Docker Compose

---

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 15+ (or use Docker)
- Docker & Docker Compose (optional but recommended)
- Git

---

## Local Development Setup

### Option 1: Using Docker Compose (Recommended)

```bash
# Clone the repository
git clone <repository-url>
cd upms-backend

# Start all services
docker-compose up -d

# Verify services are running
docker ps

# Check logs
docker-compose logs -f
```

**Access Points:**
- Auth Service: http://localhost:8081
- Procurement Service: http://localhost:8082
- Bidding Service: http://localhost:8083
- Evaluation Service: http://localhost:8084
- PgAdmin: http://localhost:5050 (email: admin@upms.local, password: admin)

---

### Option 2: Manual Setup

#### 1. Setup PostgreSQL Databases

```bash
# Create databases
createdb -U postgres upms_auth
createdb -U postgres upms_procurement
createdb -U postgres upms_bidding
createdb -U postgres upms_evaluation
```

#### 2. Build All Services

```bash
# Build Auth Service
cd auth
mvn clean package -DskipTests

# Build Procurement Service
cd ../procurement
mvn clean package -DskipTests

# Build Bidding Service
cd ../bidding
mvn clean package -DskipTests

# Build Evaluation Service
cd ../evaluation
mvn clean package -DskipTests
```

#### 3. Run Services

```bash
# Terminal 1 - Auth Service
cd auth
mvn spring-boot:run

# Terminal 2 - Procurement Service
cd procurement
mvn spring-boot:run

# Terminal 3 - Bidding Service
cd bidding
mvn spring-boot:run

# Terminal 4 - Evaluation Service
cd evaluation
mvn spring-boot:run
```

---

## API Documentation

### Swagger/OpenAPI UI

Each service exposes OpenAPI documentation at:

- Auth Service: http://localhost:8081/swagger-ui.html
- Procurement Service: http://localhost:8082/swagger-ui.html
- Bidding Service: http://localhost:8083/swagger-ui.html
- Evaluation Service: http://localhost:8084/swagger-ui.html

---

## Authentication

### JWT Token Format

```
Header.Payload.Signature
```

**Example Token:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJyb2xlcyI6WyJQUk9DVVJFTUVOVF9PRkZJQ0VSIl0sImlhdCI6MTcwNDY2NjAwMCwiZXhwIjoxNzA0NjY5NjAwfQ.signature
```

### Using JWT in Requests

Add Authorization header:

```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
  http://localhost:8082/api/v1/procurement/rfqs
```

### Login Flow

```bash
# 1. Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user@example.com",
    "password": "password123"
  }'

# Response includes:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "refresh_token_value",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 1,
  "roles": ["PROCUREMENT_OFFICER"]
}

# 2. Use accessToken in subsequent requests
# 3. When accessToken expires, use refreshToken to get new token
curl -X POST http://localhost:8081/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "refresh_token_value"}'
```

---

## User Roles and Permissions

### Available Roles

1. **ADMIN** - System Administrator (full access)
2. **PROCUREMENT_OFFICER** - Procurement officer (create RFQs, manage procurements)
3. **BIDDER** - Supplier/Bidder (submit bids)
4. **EVALUATOR** - Bid evaluator (evaluate bids)
5. **TENDER_BOARD_MEMBER** - Tender board member (approve procurements)
6. **HOD** - Head Of Department (approve up to specific value)
7. **FACULTY_BURSAR** - Faculty Bursar (approve faculty procurements)
8. **FACULTY_DEAN** - Faculty Dean (approve faculty procurements)
9. **BURSAR** - Central Bursar (approve high-value procurements)
10. **SUPPLIER_DIVISION_CLERK** - Manage suppliers
11. **TEC_MEMBER** - Technical Evaluation Committee member (technical evaluation)
12. **STORE_KEEPER** - Receive goods and manage inventory
13. **FINANCE_DIVISION** - Finance division staff (payment processing)

---

## Procurement Flow

### Step 1: Create Procurement (RFQ)

```bash
POST /api/v1/procurement/create
{
  "title": "Office Supplies",
  "description": "Annual office supplies procurement",
  "estimatedValue": 500000,
  "currency": "LKR",
  "procurementMethodId": 1,
  "openingDate": "2024-02-15T10:00:00",
  "closingDate": "2024-02-28T16:00:00",
  "documentFee": 8000
}
```

**System automatically determines procurement method based on value:**
- < LKR 25M (Local) / 40M (Foreign): National Shopping Method
- > Thresholds: National Competitive Bidding (requires newspaper + promise.lk posting)

---

### Step 2: Send RFQs or Publish Tender

```bash
# For NSM - Send to specific suppliers
POST /api/v1/procurement/{procurementId}/send-rfq
{
  "supplierIds": [1, 2, 3]
}

# For NCB - Publish to newspapers and promise.lk
POST /api/v1/procurement/{procurementId}/publish-newspapers
{
  "newspaperPublications": [
    {
      "newspaperName": "The Island",
      "language": "ENGLISH",
      "publicationDate": "2024-02-15"
    }
  ]
}

POST /api/v1/procurement/{procurementId}/post-to-promise-lk
{
  "postDate": "2024-02-15"
}
```

---

### Step 3: Bidders Submit Bids

```bash
POST /api/v1/bidding/submit-bid
{
  "procurementId": 1,
  "bidderId": 10,
  "financialBid": 450000,
  "deliverySchedule": "30 days from PO",
  "warrantyPeriod": "1 year",
  "paymentTerms": "Net 30"
}
```

**Mandatory Requirements:**
- Bid Bond: Valid for 120 days (auto-rejected if missing)
- VAT/SSCL Declaration: Must declare registration number
- Document Fee: LKR 8,000 or 12,500 (non-refundable)

---

### Step 4: Bid Evaluation

#### 4a. Preliminary Examination

```bash
POST /api/v1/evaluation/preliminary-examination/{bidId}
{
  "isComplete": true,
  "hasAllSignatures": true,
  "bidBondValid": true,
  "vatSsclCompliant": true,
  "documentFeePaid": true,
  "examinationNotes": "All requirements met"
}
```

#### 4b. Technical Evaluation (for procurements > LKR 500K)

```bash
POST /api/v1/evaluation/technical-evaluation/{bidId}
{
  "scores": [
    {
      "criterionId": 1,
      "score": 90,
      "comments": "Excellent compliance"
    }
  ]
}
```

#### 4c. Financial Evaluation

```bash
POST /api/v1/evaluation/financial-evaluation
{
  "procurementId": 1,
  "bids": [
    {"bidId": 1, "rank": 1},
    {"bidId": 2, "rank": 2}
  ]
}
```

---

### Step 5: Approval Routing (Value-based)

System automatically routes BES (Bid Evaluation Summary) report to appropriate authority:

```
< LKR 500K → Dean + HOD approval
LKR 500K - X → Faculty Board approval
X - Y → Management Board approval
> Y → University Board/Council approval
```

---

### Step 6: Purchase Order & Contract

```bash
POST /api/v1/evaluation/generate-purchase-order
{
  "procurementId": 1,
  "recommendedBidderId": 1
}

# Response: Purchase Order created with:
# - PO Number auto-generated
# - Supplier details
# - Performance Bond requirements
# - Payment terms
```

---

### Step 7: Goods Receipt & Quality

```bash
# Storekeeper creates GRN
POST /api/v1/evaluation/goods-received-note
{
  "poId": 1,
  "receivedDate": "2024-03-10",
  "storageLocation": "Warehouse A"
}

# HOD creates Quality Report
POST /api/v1/evaluation/quality-report/{grnId}
{
  "qualityStatus": "APPROVED",
  "inspectionNotes": "All items as per specification"
}
```

---

### Step 8: Payment Processing

```bash
# Finance division tracks and processes payment
GET /api/v1/evaluation/payment-tracking/{poId}

POST /api/v1/evaluation/process-payment/{paymentId}
{
  "paymentDate": "2024-03-15",
  "paymentMethod": "BANK_TRANSFER",
  "referenceNumber": "TXN-2024-001"
}

# System monitors SLA:
# - Alerts if payment delayed
# - Tracks supplier satisfaction
# - Maintains audit trail
```

---

## Security Best Practices

### 1. JWT Security

- **Secret Key:** Change `app.jwt.secret` in production
- **Token Expiration:** Default 1 hour (configurable)
- **Refresh Token:** Valid for 24 hours, auto-revoked after use
- **Signature:** HS256 algorithm

### 2. Database Security

- **Encryption:** Configure PostgreSQL SSL for connections
- **Credentials:** Use environment variables (never hardcode)
- **Backups:** Regular automated backups with encryption
- **Access Control:** Implement role-based database permissions

### 3. API Security

- **CORS:** Configured for localhost (update in production)
- **Rate Limiting:** Implement per client (via API Gateway)
- **Input Validation:** All inputs validated with annotations
- **SQL Injection:** Protected via JPA/Hibernate parameterized queries
- **CSRF:** Implement CSRF tokens for state-changing operations

### 4. Audit Logging

- All user actions logged to `audit_logs` table
- Sensitive operations require additional authorization
- Audit trails retained for compliance

### 5. Azure AD Integration

```properties
# Configure in application.properties
azure.ad.tenant-id=your-tenant-id
azure.ad.client-id=your-client-id
azure.ad.client-secret=your-client-secret
azure.ad.redirect-uri=http://localhost:8081/auth/azure-callback
```

**Setup Steps:**
1. Register application in Azure AD
2. Create client secret
3. Configure redirect URI
4. Grant API permissions to Microsoft Graph

---

## Database Migrations (Flyway)

### Migration Files Location

```
auth/src/main/resources/db/migration/V1__Initial_Auth_Schema.sql
procurement/src/main/resources/db/migration/V1__Initial_Procurement_Schema.sql
bidding/src/main/resources/db/migration/V1__Initial_Bidding_Schema.sql
evaluation/src/main/resources/db/migration/V1__Initial_Evaluation_Schema.sql
```

### Running Migrations

Migrations run automatically on service startup. To create new migration:

```bash
# Create new migration file
# V2__Add_New_Column.sql

# Flyway will automatically execute on next service start
```

---

## Inter-Service Communication

### Service URLs Configuration

```properties
# In each service's application.properties
auth.service.url=http://localhost:8081
procurement.service.url=http://localhost:8082
bidding.service.url=http://localhost:8083
evaluation.service.url=http://localhost:8084
```

### OpenFeign Client Example

```java
@FeignClient(name = "procurement-service", url = "${procurement.service.url}")
public interface ProcurementClient {
    @GetMapping("/api/v1/procurement/{id}")
    ProcurementDTO getProcurement(@PathVariable Long id);
}
```

---

## Monitoring and Logging

### Logging Levels

```properties
logging.level.root=INFO
logging.level.com.v1.auth=DEBUG
logging.level.com.v1.procurement=DEBUG
```

### Health Checks

```bash
curl http://localhost:8081/api/v1/auth/health
curl http://localhost:8082/api/v1/procurement/health
curl http://localhost:8083/api/v1/bidding/health
curl http://localhost:8084/api/v1/evaluation/health
```

---

## Deployment (Production)

### Environment Variables

```bash
# Core
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8081

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://db.production.com:5432/upms_auth
SPRING_DATASOURCE_USERNAME=<username>
SPRING_DATASOURCE_PASSWORD=<password>

# JWT
APP_JWT_SECRET=<strong-random-secret>
APP_JWT_EXPIRATION=3600000

# Azure AD
AZURE_AD_TENANT_ID=<your-tenant-id>
AZURE_AD_CLIENT_ID=<your-client-id>
AZURE_AD_CLIENT_SECRET=<your-client-secret>

# SSL/TLS
SERVER_SSL_ENABLED=true
SERVER_SSL_KEY_STORE=classpath:keystore.p12
SERVER_SSL_KEY_STORE_PASSWORD=<password>
```

### Docker Build & Push

```bash
# Build Docker image
docker build -t upms/auth-service:1.0 ./auth

# Push to registry
docker push upms/auth-service:1.0

# Deploy to Kubernetes (if applicable)
kubectl apply -f k8s-deployment.yml
```

---

## Troubleshooting

### Port Already in Use

```bash
# Find process using port
lsof -i :8081

# Kill process
kill -9 <PID>
```

### Database Connection Issues

```bash
# Test database connection
psql -h localhost -U postgres -d upms_auth

# Check PostgreSQL logs
docker logs <container-name>
```

### JWT Token Errors

- **"Token is invalid"**: Check token signature and secret
- **"Token expired"**: Use refresh token to get new token
- **"Missing Authorization header"**: Include `Authorization: Bearer <token>` header

---

## Support and Contact

- **Email:** upms-support@university.edu
- **Documentation:** See `/Docs` folder
- **Issue Tracking:** GitHub Issues

---

**Last Updated:** 2024-02-17
**Version:** 1.0.0
