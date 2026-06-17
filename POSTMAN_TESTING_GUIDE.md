# Postman Testing Guide - UPMS Backend

## Overview

This guide explains how to use the provided API files to test the UPMS Backend microservices using Postman.

## Files Provided

1. **`openapi.json`** - OpenAPI 3.0 specification of all APIs
2. **`POSTMAN_COLLECTION.json`** - Ready-to-use Postman collection with sample requests

## Prerequisites

- Postman installed (https://www.postman.com/downloads/)
- All Docker containers running: `docker-compose up -d`
- All four services should be healthy

## Quick Start (3 Steps)

### Step 1: Import Postman Collection

1. Open Postman
2. Click **"File"** → **"Import"**
3. Select **"POSTMAN_COLLECTION.json"**
4. Click **"Import"**
5. You should see "UPMS Backend API" collection in the left sidebar

### Step 2: Get Authentication Token

1. Expand **"Authentication"** folder
2. Click **"Login"** request
3. Click **"Send"**
4. Copy the `accessToken` from response
5. In Postman, click **"Variables"** (top right)
6. Paste token in `accessToken` field
7. Click **"Save"**

**Example Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Step 3: Start Testing

All requests are now ready to use with authentication!

## Testing Workflow

### 1. Authentication Tests

**Test Login:**
- Request: `Authentication` → `Login`
- Method: `POST`
- Endpoint: `/api/v1/auth/login`
- Body:
  ```json
  {
    "username": "test@upms.local",
    "password": "password123"
  }
  ```

**Verify Token:**
- Request: `Authentication` → `Verify Token`
- Method: `GET`
- Will check if your current token is valid

**Refresh Token:**
- Request: `Authentication` → `Refresh Token`
- Use the refreshToken to get new accessToken

### 2. Procurement Tests

#### Create Procurement
- **Request:** `Procurement` → `Create Procurement`
- **Method:** `POST`
- **Endpoint:** `/api/v1/procurement/create`
- **Body Example:**
  ```json
  {
    "title": "Purchase of Office Supplies",
    "description": "Procurement for office supplies and stationery",
    "estimatedValue": 500000.00,
    "procurementMethodId": 1,
    "procurementCategoryId": 1,
    "openingDate": "2024-02-20T10:00:00",
    "closingDate": "2024-02-25T10:00:00",
    "documentFee": 8000.00,
    "requiresBidBond": true,
    "bidBondPercentage": 5.0
  }
  ```
- **Response:** Returns procurement with ID and reference number

#### Get Procurement
- **Request:** `Procurement` → `Get Procurement`
- **Endpoint:** `/api/v1/procurement/{id}`
- **Replace `{id}` with actual procurement ID from previous response**

#### List All Procurements
- **Request:** `Procurement` → `List All Procurements`
- **Supports pagination:** `page=0&size=10`

#### Update Procurement
- **Request:** `Procurement` → `Update Procurement`
- **Method:** `PUT`
- **Note:** Only DRAFT procurements can be updated

#### Publish Procurement
- **Request:** `Procurement` → `Publish Procurement`
- **Method:** `POST`
- **Body:**
  ```json
  {
    "publish": true,
    "rfqRecipientEmails": ["supplier1@company.com", "supplier2@company.com"],
    "postOnPromiseLk": true,
    "publishInNewspaper": false
  }
  ```

#### Add RFQ Recipients
- **Request:** `Procurement` → `Add RFQ Recipient`
- **Method:** `POST`
- **Body:**
  ```json
  {
    "supplierEmail": "supplier@company.com",
    "supplierName": "ABC Supplies Ltd",
    "supplierId": "SUP-001"
  }
  ```

### 3. Bidding Tests

#### Submit Bid
- **Request:** `Bidding` → `Submit Bid`
- **Method:** `POST`
- **Endpoint:** `/api/v1/bidding/{procurementId}/submit-bid`
- **Replace `{procurementId}` with actual procurement ID**
- **Body:**
  ```json
  {
    "procurementId": 1,
    "financialBid": 450000.00,
    "deliverySchedule": "15 days from PO",
    "warrantyPeriod": "1 year",
    "paymentTerms": "Net 30",
    "bidBond": {
      "bondReference": "BD-2024-001",
      "issuerBank": "Commercial Bank of Ceylon",
      "bondAmount": 50000.00,
      "issuedDate": "2024-02-01",
      "expiryDate": "2024-08-01"
    },
    "documentFeeReference": "PAY-2024-001",
    "vatSsclDeclaration": {
      "registrationNumber": "123456789",
      "registrationType": "VAT",
      "isDeclared": true
    }
  }
  ```

**Important:** 
- Bid bond expiry must be **at least 120 days** from today
- VAT/SSCL must be **declared = true**
- Document fee reference is required

#### Get Bid Details
- **Request:** `Bidding` → `Get Bid Details`
- **Replace bid ID in URL**

#### List Bids for Procurement
- **Request:** `Bidding` → `List Bids for Procurement`
- **Endpoint:** `/api/v1/bidding/procurement/{procurementId}/bids`

#### Get My Bids
- **Request:** `Bidding` → `Get My Bids`
- **Lists all bids submitted by current bidder**

#### Get Compliant Bids
- **Request:** `Bidding` → `Get Compliant Bids`
- **Only shows bids that passed all validations**

#### Reject Bid
- **Request:** `Bidding` → `Reject Bid`
- **Method:** `POST`
- **Body:**
  ```json
  {
    "reason": "Bid bond validity less than 120 days",
    "category": "COMPLIANCE"
  }
  ```

#### Submit Amendment
- **Request:** `Bidding` → `Submit Bid Amendment`
- **Body:**
  ```json
  {
    "type": "PRICE_REVISION",
    "description": "Revised quote based on market rates"
  }
  ```

## Postman Variables

The collection uses two variables for authentication:

| Variable | Description |
|----------|-------------|
| `{{accessToken}}` | JWT access token (1 hour validity) |
| `{{refreshToken}}` | Refresh token (24 hour validity) |

**How to Set Variables:**
1. Click **"Variables"** at top right of Postman
2. Set values in **"CURRENT VALUE"** column
3. Click **"Save"** or press `Ctrl+S`

## Common Test Scenarios

### Scenario 1: Complete Procurement Flow

1. **Login** → Get accessToken
2. **Create Procurement** → Get procurementId
3. **Publish Procurement** → Send RFQs to suppliers
4. **List Procurements by Status** → Verify status changed to PUBLISHED

### Scenario 2: Complete Bidding Flow

1. **List Active Procurements** → Find one you want to bid on
2. **Submit Bid** → Include all required documents (bond, VAT/SSCL, fee)
3. **Get Bid Details** → Verify bid was accepted
4. **Get My Bids** → See all your submitted bids

### Scenario 3: Bid Management

1. **Get Compliant Bids** → See only valid bids for a procurement
2. **Reject Bid** → If validation failed
3. **Get Bid Bond** → View bond details
4. **Get VAT/SSCL** → View tax registration

## Testing Different Procurement Methods

The system **automatically selects** procurement method based on estimated value:

| Method | Value Range | Publishing |
|--------|------------|-----------|
| NSM | < 25M | Direct to suppliers |
| NCB | 25M - 500M | Newspapers + Promise.lk |
| LCB | > 500M | Newspapers + Promise.lk + DB |

**Test Examples:**
- Small: 10,000,000 (NSM)
- Medium: 100,000,000 (NCB)
- Large: 600,000,000 (LCB)

## Validation Rules to Test

### Bid Bond Validation
✅ **Must pass:**
- Expiry date at least 120 days from today
- Amount > 0
- Valid issuer bank

❌ **Will fail:**
- Expiry date < 120 days
- Missing bond details
- Invalid bank

### VAT/SSCL Validation
✅ **Must pass:**
- `isDeclared: true`
- Valid registration number
- Registration type: VAT or SSCL

❌ **Will fail:**
- `isDeclared: false` (automatic rejection)
- Missing registration number
- Invalid format

### Financial Bid Validation
✅ **Must pass:**
- Amount > 0
- Valid number format

❌ **Will fail:**
- Amount ≤ 0
- Non-numeric value

## Debugging Tips

### View Full Request/Response
1. Click request
2. Scroll down to see request body and headers
3. After sending, see full response in lower panel

### Check Token Validity
- Use `Authentication` → `Verify Token` request
- If expired, use `Refresh Token` request

### See Error Details
- Look at response body for error messages
- Most validations return detailed error text

### Enable Postman Console
- `Ctrl+Alt+C` (Windows) or `Cmd+Option+C` (Mac)
- Shows all HTTP requests/responses in detail

## Health Check Endpoints

Before testing, verify all services are running:

```
GET http://localhost:8081/api/v1/auth/health
GET http://localhost:8082/api/v1/procurement/health
GET http://localhost:8083/api/v1/bidding/health
GET http://localhost:8084/api/v1/evaluation/health
```

All should return: `"Service is running"`

## Troubleshooting

### Issue: 401 Unauthorized
**Solution:** 
- Token expired → Use Refresh Token request
- Token not set → Copy token to variables
- Token in wrong format → Should be `Bearer {{accessToken}}`

### Issue: 404 Not Found
**Solution:**
- ID doesn't exist → Verify with List requests
- Wrong port → Check service is on correct port (8081-8084)
- Wrong endpoint → Copy from collection

### Issue: 400 Bad Request
**Solution:**
- Missing required fields → Check request body
- Invalid data format → Check dates are ISO format
- Validation failed → Check validation rules above

### Issue: Connection Refused
**Solution:**
- Docker containers not running → `docker-compose up -d`
- Wrong port → Check port in URL
- Service crashed → `docker-compose logs servicename`

## Next Steps

After successful testing:

1. Export results as report
2. Create automated test suites
3. Set up continuous integration tests
4. Document business requirements
5. Build frontend integration

## Additional Resources

- **API Documentation:** See `API_DOCUMENTATION.md`
- **Database Schema:** See `IMPLEMENTATION_SUMMARY.md`
- **OpenAPI Spec:** Import `openapi.json` to any OpenAPI client
- **Docker Logs:** `docker-compose logs -f servicename`

## Support

For issues or questions:
1. Check Postman Console for detailed errors
2. Review Docker logs: `docker-compose logs`
3. Verify all services are running
4. Check token hasn't expired

---

**Happy Testing! 🚀**
