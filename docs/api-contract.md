# KampungHub API Contract Specification

This document defines the REST API contract for the **KampungHub Smart Neighborhood Security System**. All requests and responses use the `application/json` content type unless specified otherwise.

---

## 1. Base Configuration & Global Schemes

### 1.1 Base URL
* **Development Backend**: `http://localhost:8080`
* **API Version prefix**: `/api/v1`

### 1.2 Access Headers & JWT Authentication Scheme
For role verification and access control, all restricted requests must include the standard HTTP header:
* `Authorization: Bearer <JWT_TOKEN>`

The JWT token contains the authenticated user's platform identity (`userId`, `email`, `fullName`) and their active neighborhood context (`neighborhoodId`, `membershipId`, and `roles` array).

### 1.3 Authentication & Session Endpoints

#### 1.3.1 User Login (Authenticate)
* **Endpoint**: `POST /api/v1/auth/login`
* **Access Control**: Public
* **Description**: Authenticate using platform-wide credentials. The backend automatically binds the session to a default neighborhood (e.g. the preferred choice from local cache or the first active membership in their profile). The response contains the bound JWT token, active membership context, and a list of all other available memberships to populate the frontend switch selector dropdown.
* **Request Body**:
  ```json
  {
    "email": "ahmad@example.com",
    "password": "securepassword123",
    "preferredMembershipId": "mem-usj4-001"
  }
  ```
* **Success Response (200 OK)**:
  ```json
  {
    "token": "jwt-token-string-bound-to-active-membership",
    "userId": "usr-001",
    "email": "ahmad@example.com",
    "fullName": "Ahmad Zulkifli",
    "activeMembership": {
      "membershipId": "mem-usj4-001",
      "neighborhoodId": "nh-usj4-001",
      "neighborhoodName": "Taman USJ 4",
      "unitNumber": "No. 12 Jalan USJ 4/1",
      "roles": ["OWNER", "ADMIN"]
    },
    "memberships": [
      {
        "membershipId": "mem-usj4-001",
        "neighborhoodId": "nh-usj4-001",
        "neighborhoodName": "Taman USJ 4",
        "unitNumber": "No. 12 Jalan USJ 4/1",
        "roles": ["OWNER", "ADMIN"]
      },
      {
        "membershipId": "mem-ss15-999",
        "neighborhoodId": "nh-ss15-002",
        "neighborhoodName": "SS15 Condominium",
        "unitNumber": "A-12-05",
        "roles": ["TENANT"]
      }
    ]
  }
  ```

#### 1.3.2 Select Neighborhood Context
* **Endpoint**: `POST /api/v1/auth/context`
* **Access Control**: Public (Pre-authenticated user session context)
* **Description**: Obtain a new JWT token bound to the selected neighborhood membership (called when switching context from the navigation dropdown).
* **Request Body**:
  ```json
  {
    "userId": "usr-001",
    "membershipId": "mem-ss15-999"
  }
  ```
* **Success Response (200 OK)**:
  ```json
  {
    "token": "jwt-token-string-bound-to-ss15",
    "userId": "usr-001",
    "email": "ahmad@example.com",
    "fullName": "Ahmad Zulkifli",
    "activeMembership": {
      "membershipId": "mem-ss15-999",
      "neighborhoodId": "nh-ss15-002",
      "neighborhoodName": "SS15 Condominium",
      "unitNumber": "A-12-05",
      "roles": ["TENANT"]
    }
  }
  ```

### 1.4 Global Error Format
When an API request fails, the server returns a standard error body matching `ApiErrorResponse.java`:

**HTTP Status Codes**:
* `400 Bad Request`: Validation failure or malformed JSON payload.
* `401 Unauthorized`: Missing or invalid identification header or token.
* `403 Forbidden`: Insufficient role permissions.
* `404 Not Found`: Resource does not exist.
* `500 Internal Server Error`: Unexpected backend exceptions.

**Payload Schema**:
```json
{
  "statusCode": 400,
  "error": "Bad Request - Validation Error",
  "message": "plateText: Invalid Malaysian plate format (e.g. VHM8807 or W1234A).",
  "path": "/api/v1/vehicles",
  "timestamp": "2026-07-30T17:15:52.456"
}
```

---

## 2. Neighborhood Management Endpoints

### 2.1 List Neighborhoods
* **Endpoint**: `GET /api/v1/neighborhoods`
* **Access Control**: `SYSTEM_ADMIN` only
* **Query Parameters**:
  * `tier` (optional): Filter by subscription tier (`BASIC_LANDED`, `PREMIUM_LANDED`, etc.)
  * `status` (optional): Filter by subscription status (`ACTIVE`, `EXPIRED`)
* **Success Response (200 OK)**:
  ```json
  [
    {
      "id": "nh-usj4-001",
      "name": "Taman USJ 4",
      "propertyType": "LANDED",
      "subscriptionTier": "PREMIUM_LANDED",
      "subscriptionStatus": "ACTIVE",
      "maxAllowedUnits": 350,
      "createdAt": "2026-07-30T06:06:39"
    }
  ]
  ```

### 2.2 Get Neighborhood Details
* **Endpoint**: `GET /api/v1/neighborhoods/{id}`
* **Access Control**: `SYSTEM_ADMIN`, `NEIGHBORHOOD_ADMIN`
* **Success Response (200 OK)**:
  ```json
  {
    "id": "nh-usj4-001",
    "name": "Taman USJ 4",
    "propertyType": "LANDED",
    "subscriptionTier": "PREMIUM_LANDED",
    "subscriptionStatus": "ACTIVE",
    "maxAllowedUnits": 350,
    "createdAt": "2026-07-30T06:06:39"
  }
  ```

### 2.3 Register Neighborhood
* **Endpoint**: `POST /api/v1/neighborhoods`
* **Access Control**: `SYSTEM_ADMIN`
* **Request Body**:
  ```json
  {
    "name": "Taman USJ 4",
    "propertyType": "LANDED",
    "subscriptionTier": "PREMIUM_LANDED",
    "maxAllowedUnits": 350
  }
  ```
* **Success Response (201 Created)**:
  ```json
  {
    "id": "nh-usj4-001",
    "name": "Taman USJ 4",
    "propertyType": "LANDED",
    "subscriptionTier": "PREMIUM_LANDED",
    "subscriptionStatus": "ACTIVE",
    "maxAllowedUnits": 350,
    "createdAt": "2026-07-30T17:15:52"
  }
  ```

---

## 3. Members & Onboarding Endpoints

### 3.0 Bootstrapping the Neighborhood Admin
To register the first administrator for a neighborhood:
1. The **System Admin** (`SYSTEM_ADMIN`) registers the neighborhood using `POST /api/v1/neighborhoods`.
2. The **System Admin** creates/invites the initial Neighborhood Admin by calling `POST /api/v1/neighborhoods/{id}/members/invite` with `"role": "ADMIN"`.
3. The invited user receives the link, opens the public onboarding page (`/members/register?token=...`), and registers. This changes their status to `ACTIVE`.
4. From then on, this new **Neighborhood Admin** can log in, access the admin features, and invite residents (`OWNER`, `RESIDENT`, `TENANT`) or other admin assistants.

### 3.0.1 Guard Account Creation Flow
To register a new security guard for the gatehouse:
1. The **Neighborhood Admin** (`NEIGHBORHOOD_ADMIN`) invites the guard by calling `POST /api/v1/neighborhoods/{id}/members/invite` with `"role": "GUARD"` and `"unitNumber": "GUARDHOUSE"`.
2. The guard opens the onboarding registration page (`/members/register?token=...`), enters their details/password, and completes registration, which changes their status to `ACTIVE`.
3. The guard is now registered as a neighborhood member with the role `GUARD`, allowing them to log in to the Guard Dashboard (`/guard/dashboard`) and manage gate logs.

### 3.1 List Neighborhood Members
* **Endpoint**: `GET /api/v1/neighborhoods/{id}/members`
* **Access Control**: `NEIGHBORHOOD_ADMIN` (for their own neighborhood), `SYSTEM_ADMIN`
* **Query Parameters**:
  * `status` (optional): Filter by registration status (`INVITED`, `ACTIVE`, `INACTIVE`)
  * `role` (optional): Filter by household/staff role (`ADMIN`, `GUARD`, `OWNER`, `RESIDENT`, `FAMILY_MEMBER`, `TENANT`)
* **Success Response (200 OK)**:
  ```json
  [
    {
      "membershipId": "mem-usj4-001",
      "userId": "usr-001",
      "neighborhoodId": "nh-usj4-001",
      "fullName": "Ahmad Zulkifli",
      "email": "ahmad@example.com",
      "unitNumber": "No. 12 Jalan USJ 4/1",
      "phoneNumber": "012-3456789",
      "roles": ["OWNER", "ADMIN"],
      "status": "ACTIVE",
      "createdAt": "2026-07-30T06:06:39"
    }
  ]
  ```

### 3.2 Get Neighborhood Member Details
* **Endpoint**: `GET /api/v1/neighborhoods/{id}/members/{membershipId}`
* **Access Control**: `SYSTEM_ADMIN`, `NEIGHBORHOOD_ADMIN`, `RESIDENT` (self-only)
* **Success Response (200 OK)**:
  ```json
  {
    "membershipId": "mem-usj4-001",
    "userId": "usr-001",
    "neighborhoodId": "nh-usj4-001",
    "fullName": "Ahmad Zulkifli",
    "email": "ahmad@example.com",
    "unitNumber": "No. 12 Jalan USJ 4/1",
    "phoneNumber": "012-3456789",
    "roles": ["OWNER", "ADMIN"],
    "status": "ACTIVE",
    "createdAt": "2026-07-30T06:06:39"
  }
  ```

### 3.3 Register Member Directly
* **Endpoint**: `POST /api/v1/neighborhoods/{id}/members`
* **Access Control**: `NEIGHBORHOOD_ADMIN`, `SYSTEM_ADMIN`
* **Description**: Directly pre-creates an active member account (bypass invitation).
* **Request Body**:
  ```json
  {
    "fullName": "Ahmad Zulkifli",
    "email": "ahmad@example.com",
    "unitNumber": "No. 12 Jalan USJ 4/1",
    "phoneNumber": "012-3456789",
    "roles": ["OWNER", "ADMIN"]
  }
  ```
* **Success Response (201 Created)**:
  ```json
  {
    "membershipId": "mem-usj4-001",
    "userId": "usr-001",
    "neighborhoodId": "nh-usj4-001",
    "fullName": "Ahmad Zulkifli",
    "email": "ahmad@example.com",
    "unitNumber": "No. 12 Jalan USJ 4/1",
    "phoneNumber": "012-3456789",
    "roles": ["OWNER", "ADMIN"],
    "status": "ACTIVE",
    "createdAt": "2026-07-30T17:15:52"
  }
  ```

### 3.4 Invite Resident Member (Starts "INVITED")
* **Endpoint**: `POST /api/v1/neighborhoods/{id}/members/invite`
* **Access Control**: `NEIGHBORHOOD_ADMIN`, `SYSTEM_ADMIN`
* **Description**: Create a member shell and generate an invitation token for user registration.
* **Request Body**:
  ```json
  {
    "fullName": "Chong Wei",
    "email": "chong@example.com",
    "unitNumber": "No. 88 Jalan USJ 4/3",
    "roles": ["TENANT"]
  }
  ```
* **Success Response (201 Created)**:
  ```json
  {
    "membershipId": "mem-usj4-003",
    "userId": "usr-003",
    "neighborhoodId": "nh-usj4-001",
    "fullName": "Chong Wei",
    "email": "chong@example.com",
    "unitNumber": "No. 88 Jalan USJ 4/3",
    "phoneNumber": null,
    "roles": ["TENANT"],
    "status": "INVITED",
    "invitationToken": "token-chong-12345",
    "createdAt": "2026-07-30T17:15:52"
  }
  ```

### 3.5 Complete Invitation Onboarding Registration
* **Endpoint**: `POST /api/v1/members/register`
* **Access Control**: Public (Onboarding Token verification)
* **Query Parameters**:
  * `token`: The invitation token generated by the admin (`token-chong-12345`)
* **Request Body**:
  ```json
  {
    "fullName": "Chong Wei",
    "phoneNumber": "012-5555555",
    "password": "securepassword123"
  }
  ```
* **Success Response (200 OK)**:
  ```json
  {
    "membershipId": "mem-usj4-003",
    "userId": "usr-003",
    "neighborhoodId": "nh-usj4-001",
    "fullName": "Chong Wei",
    "email": "chong@example.com",
    "unitNumber": "No. 88 Jalan USJ 4/3",
    "phoneNumber": "012-5555555",
    "roles": ["TENANT"],
    "status": "ACTIVE",
    "createdAt": "2026-07-30T17:15:52"
  }
  ```

### 3.6 Update Member Details
* **Endpoint**: `PUT /api/v1/neighborhoods/{id}/members/{membershipId}`
* **Access Control**: `NEIGHBORHOOD_ADMIN`, `RESIDENT` (self-profile only)
* **Request Body**:
  ```json
  {
    "fullName": "Ahmad Zulkifli",
    "phoneNumber": "012-7777777",
    "status": "ACTIVE",
    "roles": ["OWNER", "ADMIN"]
  }
  ```
* **Success Response (200 OK)**:
  ```json
  {
    "membershipId": "mem-usj4-001",
    "userId": "usr-001",
    "neighborhoodId": "nh-usj4-001",
    "fullName": "Ahmad Zulkifli",
    "email": "ahmad@example.com",
    "unitNumber": "No. 12 Jalan USJ 4/1",
    "phoneNumber": "012-7777777",
    "roles": ["OWNER", "ADMIN"],
    "status": "ACTIVE",
    "createdAt": "2026-07-30T06:06:39"
  }
  ```

### 3.7 Remove Member
* **Endpoint**: `DELETE /api/v1/neighborhoods/{id}/members/{membershipId}`
* **Access Control**: `NEIGHBORHOOD_ADMIN`
* **Success Response (204 No Content)**:
  *(Empty body)*

---

## 4. Vehicle Whitelist Management Endpoints

### 4.1 Fetch Whitelisted Vehicles
* **Endpoint**: `GET /api/v1/vehicles`
* **Access Control**: `NEIGHBORHOOD_ADMIN`, `GUARD`, `RESIDENT`
* **Query Parameters**:
  * `neighborhoodId`: The neighborhood context (`nh-usj4-001`)
  * `ownerMembershipId` (optional): Filter by owner membership ID (used by admin/guard calls. For `RESIDENT` calls, the backend automatically restricts results to their own membership resolved from the JWT context).
  * `status` (optional): Filter status (`ACTIVE`, `SUSPENDED`)
  * `sortBy` (optional): Property to sort by (`plateText`, `updatedAt`, etc.)
* **Success Response (200 OK)**:
  ```json
  [
    {
      "vehicleId": "veh-001",
      "neighborhoodId": "nh-usj4-001",
      "ownerMembershipId": "mem-usj4-001",
      "unitNumber": "No. 12 Jalan USJ 4/1",
      "plateText": "VHM8807",
      "model": "Perodua Myvi",
      "color": "SILVER",
      "zoneMask": "Jalan 4/1",
      "status": "ACTIVE",
      "updatedAt": "2026-07-30T06:06:39"
    }
  ]
  ```

### 4.2 Get Specific Whitelist Entry
* **Endpoint**: `GET /api/v1/vehicles/{id}`
* **Access Control**: `NEIGHBORHOOD_ADMIN`, `GUARD`, `RESIDENT` (associated with the unit)
* **Success Response (200 OK)**:
  ```json
  {
    "vehicleId": "veh-001",
    "neighborhoodId": "nh-usj4-001",
    "ownerMembershipId": "mem-usj4-001",
    "unitNumber": "No. 12 Jalan USJ 4/1",
    "plateText": "VHM8807",
    "model": "Perodua Myvi",
    "color": "SILVER",
    "zoneMask": "Jalan 4/1",
    "status": "ACTIVE",
    "updatedAt": "2026-07-30T06:06:39"
  }
  ```

### 4.3 Register Vehicle
* **Endpoint**: `POST /api/v1/vehicles`
* **Access Control**: `NEIGHBORHOOD_ADMIN`, `RESIDENT` (must match their unit ownership)
* **Request Body**:
  ```json
  {
    "neighborhoodId": "nh-usj4-001",
    "ownerMembershipId": "mem-usj4-001",
    "unitNumber": "No. 12 Jalan USJ 4/1",
    "plateText": "VHM8807",
    "model": "Perodua Myvi",
    "color": "SILVER",
    "zoneMask": "Jalan 4/1"
  }
  ```
* **Success Response (201 Created)**:
  ```json
  {
    "vehicleId": "veh-001",
    "neighborhoodId": "nh-usj4-001",
    "ownerMembershipId": "mem-usj4-001",
    "unitNumber": "No. 12 Jalan USJ 4/1",
    "plateText": "VHM8807",
    "model": "Perodua Myvi",
    "color": "SILVER",
    "zoneMask": "Jalan 4/1",
    "status": "ACTIVE",
    "updatedAt": "2026-07-30T17:15:52"
  }
  ```

### 4.4 Update Vehicle
* **Endpoint**: `PUT /api/v1/vehicles/{id}`
* **Access Control**: `NEIGHBORHOOD_ADMIN`, `RESIDENT` (associated with the unit)
* **Request Body**:
  ```json
  {
    "model": "Perodua Myvi",
    "color": "SILVER",
    "zoneMask": "Jalan 4/1",
    "status": "SUSPENDED"
  }
  ```
* **Success Response (200 OK)**:
  ```json
  {
    "vehicleId": "veh-001",
    "neighborhoodId": "nh-usj4-001",
    "ownerMembershipId": "mem-usj4-001",
    "unitNumber": "No. 12 Jalan USJ 4/1",
    "plateText": "VHM8807",
    "model": "Perodua Myvi",
    "color": "SILVER",
    "zoneMask": "Jalan 4/1",
    "status": "SUSPENDED",
    "updatedAt": "2026-07-30T17:15:52"
  }
  ```

### 4.5 Remove Vehicle Whitelist Entry
* **Endpoint**: `DELETE /api/v1/vehicles/{id}`
* **Access Control**: `NEIGHBORHOOD_ADMIN`, `RESIDENT` (associated with the unit)
* **Success Response (204 No Content)**:
  *(Empty body)*

---

## 5. Visitor Pass Endpoints

### 5.1 Search/Verify Visitor Passes
* **Endpoint**: `GET /api/v1/visitor-passes`
* **Access Control**: `GUARD`, `NEIGHBORHOOD_ADMIN`, `RESIDENT`
* **Query Parameters**:
  * `neighborhoodId`: The neighborhood context (`nh-usj4-001`)
  * `unitNumber` (optional): Filter passes by unit number
  * `passToken` (optional): Query specific token for gatehouse lookup/scan verification
  * `status` (optional): Filter passes (`ACTIVE`, `USED`, `EXPIRED`, `CANCELLED`)
* **Success Response (200 OK)**:
  ```json
  [
    {
      "passId": "pass-001",
      "neighborhoodId": "nh-usj4-001",
      "unitNumber": "No. 12 Jalan USJ 4/1",
      "requesterMembershipId": "mem-usj4-001",
      "visitorName": "John Doe",
      "visitorPlateText": "WYY9900",
      "passToken": "vpass-token-abc12345",
      "validFrom": "2026-07-31T08:00:00",
      "validUntil": "2026-07-31T20:00:00",
      "status": "ACTIVE"
    }
  ]
  ```

### 5.2 Create Temporary Visitor Pass
* **Endpoint**: `POST /api/v1/visitor-passes`
* **Access Control**: `RESIDENT`, `NEIGHBORHOOD_ADMIN`
* **Description**: Pre-registers a visitor pass. Since it is created by an authenticated resident, the backend resolves their neighborhood, unit number, and membership ID directly from their JWT context to prevent impersonation.
* **Request Body**:
  ```json
  {
    "visitorName": "John Doe",
    "visitorPlateText": "WYY9900",
    "validFrom": "2026-07-31T08:00:00",
    "validUntil": "2026-07-31T20:00:00"
  }
  ```
* **Success Response (201 Created)**:
  ```json
  {
    "passId": "pass-001",
    "neighborhoodId": "nh-usj4-001",
    "unitNumber": "No. 12 Jalan USJ 4/1",
    "requesterMembershipId": "mem-usj4-001",
    "visitorName": "John Doe",
    "visitorPlateText": "WYY9900",
    "passToken": "vpass-token-abc12345",
    "validFrom": "2026-07-31T08:00:00",
    "validUntil": "2026-07-31T20:00:00",
    "status": "ACTIVE"
  }
  ```

### 5.3 Check-In Visitor (Verify Pass at Gate)
* **Endpoint**: `PUT /api/v1/visitor-passes/{id}/check-in`
* **Access Control**: `GUARD`
* **Description**: Used by guards at the gatehouse when a visitor arrives. Verifies the pass token, sets the status to `USED`, and automatically appends an access log entry. The guard's identity is resolved from their authenticated JWT session rather than the request body.
* **Request Body**: *(Empty body)*
* **Success Response (200 OK)**:
  ```json
  {
    "passId": "pass-001",
    "neighborhoodId": "nh-usj4-001",
    "unitNumber": "No. 12 Jalan USJ 4/1",
    "requesterMembershipId": "mem-usj4-001",
    "visitorName": "John Doe",
    "visitorPlateText": "WYY9900",
    "passToken": "vpass-token-abc12345",
    "validFrom": "2026-07-31T08:00:00",
    "validUntil": "2026-07-31T20:00:00",
    "status": "USED"
  }
  ```

---

## 6. Access Log Endpoints (Gate Logs)

### 6.1 Fetch Access Logs
* **Endpoint**: `GET /api/v1/access-logs`
* **Access Control**: `GUARD`, `NEIGHBORHOOD_ADMIN`
* **Query Parameters**:
  * `neighborhoodId`: The neighborhood context (`nh-usj4-001`)
  * `page` (optional, default: 0): Page number
  * `size` (optional, default: 20): Page size
  * `sortBy` (optional, default: `timestamp`): Sort parameter
* **Success Response (200 OK)**:
  ```json
  [
    {
      "logId": "log-001",
      "neighborhoodId": "nh-usj4-001",
      "plateText": "VHM8807",
      "accessType": "ENTRY",
      "visitorPassId": null,
      "membershipId": "mem-usj4-001",
      "verifiedByGuardId": "guard-002",
      "timestamp": "2026-07-30T10:15:00"
    },
    {
      "logId": "log-002",
      "neighborhoodId": "nh-usj4-001",
      "plateText": "WYY9900",
      "accessType": "ENTRY",
      "visitorPassId": "pass-001",
      "membershipId": null,
      "verifiedByGuardId": "guard-002",
      "timestamp": "2026-07-30T10:20:00"
    }
  ]
  ```

### 6.2 Log Gate Access Event
* **Endpoint**: `POST /api/v1/access-logs`
* **Access Control**: `GUARD`
* **Description**: Create an entry/exit checkpoint log at the guardhouse gate. The guard's identity (`verifiedByGuardId`) and neighborhood context are resolved directly from their JWT.
* **Request Body**:
  ```json
  {
    "plateText": "WYY9900",
    "accessType": "ENTRY",
    "visitorPassId": "pass-001",
    "membershipId": null
  }
  ```
* **Success Response (201 Created)**:
  ```json
  {
    "logId": "log-002",
    "neighborhoodId": "nh-usj4-001",
    "plateText": "WYY9900",
    "accessType": "ENTRY",
    "visitorPassId": "pass-001",
    "membershipId": null,
    "verifiedByGuardId": "guard-002",
    "timestamp": "2026-07-30T17:15:52"
  }
  ```
