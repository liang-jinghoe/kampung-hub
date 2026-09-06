# 🏘️ KampungHub - Smart Neighborhood Security & Access Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-17.3+-red.svg)](https://angular.io/)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.4+-blue.svg)](https://www.typescriptlang.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)

> **KampungHub** is an enterprise-grade, multi-tenant residential community security platform designed for Malaysian gated-and-guarded neighborhoods and condominiums. It automates vehicle barrier gate access with optical Automated Number Plate Recognition (ANPR) whitelist synchronization, self-service visitor QR pass generation, invitation-based resident onboarding, and real-time gatehouse security logging.

---

## 🏛️ System Architecture

KampungHub is architected as a decoupled, multi-tenant system:

```
                      ┌────────────────────────────────────────┐
                      │      Angular 17+ Standalone SPA        │
                      │  (Emerald/Slate Glassmorphic Theme)    │
                      └──────────────────┬─────────────────────┘
                                         │  HTTP / REST (JWT + X-Neighborhood-Id)
                                         ▼
                      ┌────────────────────────────────────────┐
                      │       Spring Boot 3.2.3 Backend        │
                      │   - Spring Security 6 Stateless JWT    │
                      │   - Multi-Tenant Scoped Repositories   │
                      │   - Centralized RFC-7807 Error Handler │
                      └──────────────────┬─────────────────────┘
                                         │  JPA / Hibernate
                                         ▼
                      ┌────────────────────────────────────────┐
                      │    In-Memory H2 / Production RDBMS     │
                      │    (Pre-seeded Multi-tenant Records)   │
                      └────────────────────────────────────────┘
```

---

## 🚀 Quick Start with Docker (Recommended)

Run the entire full-stack application (Backend + Frontend + Nginx) with a single command:

```bash
# Clone and navigate to repository
cd kampung-hub

# Build and start all containers
docker compose up --build
```

### 🌐 Access URLs:
* **Frontend Application:** [http://localhost:4200](http://localhost:4200)
* **Backend REST API:** [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
* **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:kampungdb`, User: `sa`, Password: *(empty)*)

To stop the containers:
```bash
docker compose down
```

---

## 💻 Local Development Setup (Without Docker)

### Prerequisites:
* **Java 17 JDK** (`java -version`)
* **Maven 3.8+** (`mvn -version`)
* **Node.js 18+ or 20+** & **npm** (`node -v`, `npm -v`)

### 1. Start Spring Boot Backend (Port 8080)
```powershell
cd kampung-hub-backend

# Ensure Java 17 is active in environment
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "C:\Program Files\Java\jdk-17\bin;" + $env:PATH

# Run automated integration tests (27/27 passing)
mvn test

# Start the Spring Boot application
mvn spring-boot:run
```

### 2. Start Angular Frontend (Port 4200)
```powershell
cd kampung-hub-frontend

# Install dependencies
npm install

# Start the development server
npm start
```
Open [http://localhost:4200](http://localhost:4200) in your browser.

---

## 🔐 Seed Accounts & Login Credentials

All seed accounts use the default password: **`password123`**.  
The Login page ([http://localhost:4200/login](http://localhost:4200/login)) also includes **1-Click Quick Demo Login buttons**.

| User Profile | Email | Password | Assigned Roles | Associated Communities |
| :--- | :--- | :--- | :--- | :--- |
| **Ahmad Zulkifli** | `ahmad@example.com` | `password123` | `ADMIN`, `OWNER`, `TENANT` | **Taman USJ 4** (`nh-usj4-001`) *(Admin)*<br>**SS15 Condominium** (`nh-ss15-002`) *(Tenant)* |
| **Siti Aminah** | `siti@example.com` | `password123` | `RESIDENT` | **Taman USJ 4** (`nh-usj4-001`) *(Unit: No. 45 Jalan USJ 4/2)* |
| **Guard Muthu** | `muthu@example.com` | `password123` | `GUARD` | **Taman USJ 4** (`nh-usj4-001`) *(Gatehouse Checkpoint Station)* |
| **Chong Wei** | `chong@example.com` | *(Invitation Token)* | `TENANT` / `RESIDENT` | **Taman USJ 4** *(Pending Activation)* |

---

## 🏙️ Seed Communities (Multi-Tenant Contexts)

| Community Name | Neighborhood ID | Property Type | Subscription Tier | Capacity Limit |
| :--- | :--- | :--- | :--- | :--- |
| **Taman USJ 4** | `nh-usj4-001` | `LANDED` | `PREMIUM_LANDED` | 350 Units |
| **SS15 Condominium** | `nh-ss15-002` | `HIGH_RISE` | `BASIC_LANDED` | 200 Units |

---

## 🎮 Step-by-Step Feature Walkthrough Guide

Follow these 4 user journeys to test all features and business logic:

### 🌟 Journey 1: Multi-Neighborhood Context Switching (Admin)
1. Navigate to [http://localhost:4200/login](http://localhost:4200/login).
2. Click the **"Ahmad"** Quick Login button (or enter `ahmad@example.com` / `password123`).
3. Notice the **Active Neighborhood Switcher** dropdown in the top Navbar showing **Taman USJ 4** (Role: `ADMIN`).
4. Click the dropdown and select **SS15 Condominium**.
5. The application seamlessly calls `AuthService.switchContext(...)`, displays the transition overlay, re-issues your JWT token for SS15 Condominium, and adapts your role to `TENANT` with updated whitelist data.

### 🚗 Journey 2: Whitelisted Vehicle Management & Malaysian Plate Regex
1. Log in as **Siti** (`siti@example.com` / `password123`).
2. Go to **Vehicles** ([http://localhost:4200/vehicles](http://localhost:4200/vehicles)).
3. Click **"Register Vehicle"**:
   * Enter a Malaysian plate number: `WYY 8888 A` or `VAA 1234` (notice the real-time uppercase formatting and regex pattern validation).
   * Enter Model (`Proton X50`) and Color (`Dark Grey`).
   * Click **"Register Vehicle"**.
4. The new vehicle appears rendered as an authentic **Malaysian License Plate card**.
5. Click **"Suspend"** on a vehicle card to temporarily block automated barrier access, or **"Edit"** to update vehicle attributes.

### 👥 Journey 3: Invitation-based Resident Onboarding & Activation
1. Log in as **Ahmad** (`ahmad@example.com`).
2. Go to **Members** ([http://localhost:4200/members](http://localhost:4200/members)).
3. Click **"Invite Resident"**:
   * Fill in Email (`newresident@example.com`), Name (`Nurul Izzah`), Unit (`No. 99 Jalan USJ 4/4`), and Role (`RESIDENT`).
   * Click **"Generate Invitation Link"**.
4. An invitation token is generated with a **1-Click Copy Link** (`http://localhost:4200/register?token=...`).
5. Open the link in a browser (or test existing token: `http://localhost:4200/register?token=token-chong-12345`).
6. Complete the onboarding form with full name, phone number, and matching passwords.
7. Click **"Complete Onboarding"** $\rightarrow$ The profile is activated to `ACTIVE` and ready to sign in!

### 🛡️ Journey 4: Visitor Pass Creation & Gatehouse Checkpoint Check-in
1. Log in as **Siti** (`siti@example.com`).
2. Go to **Visitor Passes** ([http://localhost:4200/visitor-passes](http://localhost:4200/visitor-passes)).
3. Click **"Generate Pass"**:
   * Enter Guest Name (`GrabFood Delivery`), Vehicle Plate (`B 4567 B`), and validity window.
   * Click **"Generate Pass Token"**.
4. A pass with token `vpass-token-...` is created. Click **"Share"** to inspect the simulated QR Guest Pass.
5. Log in as **Guard Muthu** (`muthu@example.com`).
6. Go to **Gatehouse Logs** ([http://localhost:4200/access-logs](http://localhost:4200/access-logs)).
7. In the **"Gatehouse Token Verification Station"** banner, paste the visitor pass token and press **Enter** (or click **"Verify & Open Gate"**).
8. The pass is instantly transitioned to `USED`, the barrier opens, and an automated `ENTRY` checkpoint record is logged into the audit table!

---

## 📚 Technical Documentation Directory

| Document | Description |
| :--- | :--- |
| 📖 [**`docs/requirements.md`**](docs/requirements.md) | Direct mapping of all Front-End (FE-Req 1-15) and Back-End (BE-Req 1-7) requirements to source code files. |
| 📑 [**`docs/project-analysis-report.md`**](docs/project-analysis-report.md) | In-depth 3-page Technical Analysis & Evaluation Report covering REST API design, component hierarchy, RxJS state management, and UX ergonomics. |
| 📋 [**`docs/api-contract.md`**](docs/api-contract.md) | Complete OpenAPI/REST API Contract specification with request/response JSON schemas and status codes. |
| ⚙️ [**`docs/setup-backend.md`**](docs/setup-backend.md) | Backend implementation notes, architecture patterns, and entity relationships. |
| 🎨 [**`docs/setup-frontend.md`**](docs/setup-frontend.md) | Frontend standalone architecture, design tokens, and template specifications. |

---

## 🧪 Verification & Test Results

```
============================================================
BACKEND INTEGRATION TESTS (Maven / JUnit 5 / MockMvc)
============================================================
[INFO] Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS

============================================================
FRONTEND COMPILATION (Angular 17 CLI / ng build)
============================================================
Application bundle generation complete. [4.207 seconds]
Output location: dist/kampung-hub-frontend
Errors: 0, Warnings: 0
```