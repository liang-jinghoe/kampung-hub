# KampungHub Smart Neighborhood Security System - Setup Guideline Index

This directory contains the specifications, configurations, and detailed implementation requirements for the multi-tenant security checkpoint management platform **KampungHub**.

---

## System Architecture Overview

```
+-------------------------------------------------------+
|                Angular Frontend (KampungHub)         |
|  (Component Hierarchy, Reactive Forms, RxJS, Router) |
+---------------------------+---------------------------+
                            |
                    HTTP / JSON REST API
                            |
+---------------------------v---------------------------+
|             Spring Boot REST Backend Gateway          |
|  (Controllers, Services, Repositories, Exception Handler)|
+---------------------------+---------------------------+
                            |
                        Spring Data JPA
                            |
+---------------------------v---------------------------+
|              Database (H2 / PostgreSQL DB)           |
|   (Neighborhoods, Members, Units, Vehicles, Passes, Logs) |
+-------------------------------------------------------+
```

---

## Roles and Access Control Matrix

To prevent security risks and simplify gatehouse operations, KampungHub enforces a clear division of roles. The **Guard** does not have administrative rights.

| Role | Access Scope | Allowed Operations | Primary Screens / Components |
| :--- | :--- | :--- | :--- |
| **System Admin (Super Admin)** | Global Platform | Manage all neighborhoods, billing, subscription tiers, and system metrics. | Neighborhood List/Detail, Admin Dashboard |
| **Neighborhood Admin** | Single Neighborhood | Invite new residents, manage active member directories, units, and the master vehicle whitelist. | Member List/Form (Inviter), Vehicle Management, Neighborhood Detail |
| **Registered Member (Resident)** | Household/Unit Level | Accept invitation (complete registration), view household details, register/update own vehicles, and pre-register visitor passes. | Resident Dashboard, Member Register (Onboarder), Vehicle Form, Visitor Pass List/Create |
| **Security Guard** | Gatehouse Gate | Read-only verification of incoming vehicles & visitor passes; Log gate entry/exit access events. | Guard Dashboard, Visitor Verification, Access Log Viewer |

### Role Authorization Logic (API & UI Rules)
1. **Administrators (System/Neighborhood)** are the only users who can call CRUD endpoints on `/api/v1/neighborhoods`, invite members on `/api/v1/neighborhoods/{id}/members/invite`, and modify vehicle status on `/api/v1/vehicles`.
2. **Registered Members (Residents)** can register vehicles under their unit ownership and generate visitor passes (`POST /api/v1/visitor-passes`).
3. **Security Guards** are restricted to verification read endpoints (`GET /api/v1/vehicles/{id}`, `GET /api/v1/visitor-passes`) and creating entry/exit logs (`POST /api/v1/access-logs`). Guards cannot edit/delete neighborhoods, members, or vehicle whitelists.
4. **Member Onboarding Workflow (Public/Invited)**: Anyone with a valid `invitationToken` can access the public registration screen (`/members/register?token=...`) to complete registration (`POST /api/v1/members/register`), which sets their status to `ACTIVE` and enables them to log in to the Resident Dashboard.
5. **Multi-Role Association (Coexistence)**: To prevent duplicate accounts when a resident is selected to serve as the Neighborhood Admin or a Security Guard, the schema models roles as a collection of strings (`roles: Set<String>`) stored in a join table (`member_roles`). A single member can hold multiple roles simultaneously (e.g., `["OWNER", "ADMIN"]` or `["RESIDENT", "GUARD"]`). This allows them to switch dashboards or access administrative features using their single household account, completely avoiding duplicate emails or contact information in the system database.

---

## Detailed Setup & Implementation Guides

To view the implementation specifications, dependencies, requirements, and checklists for the frontend and backend, please consult the respective documentation files:

* 🎨 **Front-End Guide**: [setup-frontend.md](file:///c:/kampung-hub/docs/setup-frontend.md) (Angular 17+ Application setup)
* ⚙️ **Back-End Guide**: [setup-backend.md](file:///c:/kampung-hub/docs/setup-backend.md) (Java Spring Boot 3+ Application setup)
* 📑 **REST API Specification**: [api-contract.md](file:///c:/kampung-hub/docs/api-contract.md) (Endpoints contract definition)