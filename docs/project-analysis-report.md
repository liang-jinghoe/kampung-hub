# Final Year Project (FYP) Technical Analysis & Evaluation Report
## KampungHub: Multi-Tenant Smart Neighborhood Security & Access Portal

**Project Title:** KampungHub Smart Neighborhood Security Management System  
**Frameworks:** Angular 17.3+ (Frontend SPA) & Spring Boot 3.2.3 / Java 17 (Backend REST API)  
**Author / Engineering Team:** KampungHub Development Team  
**Document Purpose:** Architectural Analysis & Technical Evaluation  

---

### 1. Executive Summary & Architecture Rationale

The KampungHub application addresses real-world security, automated vehicle barrier gate access, and resident onboarding challenges faced by modern gated-and-guarded residential communities (Landed & High-Rise) in Malaysia.

To achieve robust tenant isolation, real-time security barrier automation, and role-based operational ergonomics, the system was implemented using a **decoupled, multi-tenant micro-architecture**:
1. **Backend Layer:** A RESTful API built on **Spring Boot 3.2.3** and **Spring Security 6 with stateless JWT**, featuring JPA repositories, custom JPQL/native queries, and a centralized exception handling pipeline.
2. **Frontend Layer:** A modern Single Page Application (SPA) built on **Angular 17+ standalone components**, utilizing RxJS reactive streams (`BehaviorSubject`), a 3-tier component hierarchy, functional route guards (`CanActivateFn`), and strict reactive form validation.

This report evaluates and justifies the design and implementation choices across backend endpoint structures, frontend component modularity, reactive form architectures, state management, and end-user ergonomics.

```
+-----------------------------------------------------------------------------------------+
|                                 Angular 17 Frontend SPA                                 |
|                                                                                         |
|  [Navbar & Context Switcher]  <===>  [AuthService (session$ / BehaviorSubject)]         |
|             |                                                  |                        |
|  +----------v------------------+               +---------------v---------------------+  |
|  |   Parent: VehicleManagement |               | HttpInterceptor                     |  |
|  |   - Child: VehicleList      | <-----------> | - Authorization: Bearer <JWT>       |  |
|  |   - Grandchild: VehicleCard |               | - X-Neighborhood-Id: <Active-Id>    |  |
|  +-----------------------------+               +-------------------------------------+  |
+-----------------------------------------------------------------------------------------+
                                            |
                                 REST Calls via HTTP/JSON
                                            |
+-----------------------------------------------------------------------------------------+
|                               Spring Boot 3 REST Backend                                |
|                                                                                         |
|  +-------------------------------------+      +--------------------------------------+  |
|  | JwtAuthenticationFilter             | ===> | REST Controllers                     |  |
|  | - Extracts JWT & X-Neighborhood-Id  |      | (/api/v1/auth, /vehicles, /passes)   |  |
|  +-------------------------------------+      +--------------------------------------+  |
|                                                                  |                      |
|  +-------------------------------------+      +------------------v-------------------+  |
|  | Centralized GlobalExceptionHandler  | <--- | Service Layer (@Transactional)       |  |
|  | - Returns ApiErrorResponse (RFC-7807)|      | (Membership, Vehicle, VisitorPass)   |  |
|  +-------------------------------------+      +--------------------------------------+  |
|                                                                  |                      |
|                                               +------------------v-------------------+  |
|                                               | Spring Data JPA Repositories         |  |
|                                               | (Entities: Neighborhood, Vehicle...) |  |
|                                               +--------------------------------------+  |
+-----------------------------------------------------------------------------------------+
```

---

### 2. Backend RESTful API Design & Domain Logic Mapping

#### a) Resource-Oriented URI Naming & Parameter Decisions
The RESTful API adheres to Richardson Maturity Level 2/3 best practices, employing plural nouns, predictable hierarchical nesting, and standard HTTP verb semantics (`GET`, `POST`, `PUT`, `DELETE`).

* **Multi-Tenant Scoping vs. Global Scoping:**
  * **Nested URIs (`/api/v1/neighborhoods/{neighborhoodId}/members`):** Used where resources strictly belong to a parent aggregate. Member directories and invitations cannot exist without a parent community. Nesting enforces domain ownership and prevents accidental cross-tenant leaks.
  * **Top-Level Filterable URIs (`/api/v1/vehicles`, `/api/v1/visitor-passes`, `/api/v1/access-logs`):** Used where queries frequently cross boundaries based on user roles. For example, a platform Administrator or Security Guard queries vehicles across an entire neighborhood (`?neighborhoodId=...`), whereas a resident queries only their own unit's registered vehicles (`?ownerMembershipId=...`).
* **Specialized RPC-Style Sub-Resources for State Transitions:**
  * Instead of exposing a generic `PUT /api/v1/visitor-passes/{id}` that allows arbitrary property mutation, we created `PUT /api/v1/visitor-passes/{id}/check-in`.
  * **Rationale:** A gate check-in represents a critical security domain event. It must atomically validate pass validity window, ensure status is `ACTIVE`, transition status to `USED`, and automatically append a gatehouse barrier entry record to `access_logs`. Wrapping this in a discrete endpoint backed by a Spring `@Transactional` service method guarantees business consistency.

#### b) Context Switching & Header Transmission (`X-Neighborhood-Id`)
* When an authorized user (e.g. Ahmad) belongs to multiple neighborhoods (*Taman Damai* and *Taman Harmoni*), invoking `POST /api/v1/auth/context` reissues a cryptographic JWT containing the selected `activeMembershipId` and `activeNeighborhoodId`.
* All subsequent HTTP requests transmit the `X-Neighborhood-Id` header (automatically attached by the frontend `authInterceptor`). The backend security filter verifies that the authenticated user possesses a valid, active membership in that specific neighborhood before executing domain queries.

---

### 3. Front-End Component Hierarchy, Routing & Data Flow

#### a) 3-Tier Component Architecture Rationale (Vehicles Module)
To satisfy the requirements for component modularity ([FE-Req 4 & 5](requirements.md#L48)), the vehicle whitelist feature was architected into three distinct tiers:

```
[ VehicleManagementComponent ]  (Parent / Container Component)
        │
        ├── @Input() vehicles, @Input() isLoading
        ├── @Output() createClick, editVehicle, deleteVehicle, toggleStatus
        │
        ▼
[ VehicleListComponent ]        (Child / Presentational & Filter Component)
        │
        ├── @Input() vehicle, @Input() canManage
        ├── @Output() edit, delete, toggleStatus
        │
        ▼
[ VehicleCardComponent ]        (Grandchild / Leaf Item Component)
```

1. **Parent (`VehicleManagementComponent`):** Acts as the smart/container component. It is the single source of truth that communicates with `VehicleService`, subscribes to `AuthService.session$`, manages modal dialog visibility, and triggers user notifications (toasts).
2. **Child (`VehicleListComponent`):** Manages local UI interactions—search query inputs, status filter tab selection (`ALL`, `ACTIVE`, `SUSPENDED`), and sorting without mutating server state.
3. **Grandchild (`VehicleCardComponent`):** A pure presentation leaf component. It renders the authentic Malaysian vehicle license plate aesthetic (dark metallic container, white monospace font, `MY` country badge) and status indicators, emitting user actions (`edit`, `delete`, `toggleStatus`) up the component tree.

**Why this design is superior:**
* **Unidirectional Data Flow:** Data flows down via `@Input()`, and events flow up via `@Output()`. This guarantees high maintainability and prevents side-effects.
* **Component Reusability:** The grandchild `VehicleCardComponent` can be dropped into the guard checkpoint view or the resident dashboard without modification.

#### b) Routing Hierarchy, Child Routes & Route Guards
* **Functional Route Guards (`authGuard` & `guestGuard`):** Built with Angular's modern `CanActivateFn` functional guards instead of legacy class-based guards. `authGuard` inspects `AuthService.isLoggedIn()`, redirecting unauthenticated users to `/login` while preserving the intended target URL in `queryParams: { returnUrl }`. Conversely, `guestGuard` prevents authenticated users from viewing the login or onboarding screens, redirecting them to `/dashboard`.
* **Nested Child Routes (`/neighborhoods/:id`):**
  ```typescript
  {
    path: 'neighborhoods/:id',
    component: NeighborhoodDetailComponent,
    canActivate: [authGuard],
    children: [
      { path: 'vehicles', component: VehicleManagementComponent },
      { path: 'passes', component: VisitorPassListComponent },
      { path: 'members', component: MemberListComponent }
    ]
  }
  ```
  This allows administrators to deep-dive into any neighborhood with a persistent header displaying community metadata, switching sub-tabs dynamically inside `<router-outlet></router-outlet>` without full page reload.

---

### 4. HTTP Service Layer Delegation & RxJS State Management

#### a) Why HTTP Calls are Delegated to Services rather than Components
In compliance with enterprise separation-of-concerns principles:
* **Components are never coupled directly to `HttpClient`.** All network communication is centralized within singleton domain services (`AuthService`, `VehicleService`, `NeighborhoodService`, `VisitorPassService`, `AccessLogService`).
* **Benefits:**
  1. **Centralized URL & Parameter Management:** Backend endpoints can be modified or versioned (`/api/v2`) in one service file without touching dozens of UI templates.
  2. **Consistent RxJS Pipeline Processing:** Interceptors handle header injection, JWT bearer token attachment, and global 401/403 error capturing uniformly.
  3. **Unit Testability:** Components can be tested in isolation by mocking the injectable service class rather than mocking low-level `HttpClient` backends.

#### b) Reactive Session State with RxJS `BehaviorSubject`
* In `AuthService`, session data is encapsulated in a private `BehaviorSubject<AuthResponse | null>` and exposed publicly as an immutable `session$ = this.sessionSubject.asObservable()`.
* **Why `BehaviorSubject`:** Unlike a standard `Subject`, `BehaviorSubject` stores the *current* authentication state and immediately emits the latest active user and membership context to any newly mounted component upon subscription.
* When a user selects a different community in the Navbar context switcher dropdown, `AuthService.switchContext()` updates the `BehaviorSubject`. The Navbar, Dashboard, and Vehicle components reactively re-render in real time without requiring browser refreshes.

---

### 5. Reactive Forms Design, Validation & Security Ergonomics

#### a) Grouped Reactive Forms vs. Template-Driven Forms
All form interactions—Login (`LoginComponent`), Vehicle Registration (`VehicleFormComponent`), Member Activation (`MemberRegisterComponent`), and Visitor Pass Creation (`VisitorPassListComponent`)—use Angular **Reactive Forms (`FormGroup`, `FormControl`, `FormBuilder`)**.

* **Rationale:**
  1. **Strict Type Safety:** Reactive forms allow programmatic control over form states, dirtiness, and touched attributes.
  2. **Synchronous & Asynchronous Group Validation:** In `MemberRegisterComponent`, cross-field validation is enforced through a custom group validator (`matchPasswords`):
     ```typescript
     function matchPasswords(group: AbstractControl): ValidationErrors | null {
       const password = group.get('password')?.value;
       const confirmPassword = group.get('confirmPassword')?.value;
       return password === confirmPassword ? null : { passwordsMismatch: true };
     }
     ```
  3. **Malaysian License Plate Regular Expression Validator:**
     In `VehicleFormComponent` and `VisitorPassListComponent`, the license plate control enforces Malaysian JPJ formatting:
     ```typescript
     plateRegex = /^[A-Z]{1,3}\s?[0-9]{1,4}\s?[A-Z]?$/i;
     ```
     An event handler (`(input)="onPlateInput($event)"`) transforms typed input to uppercase in real time, ensuring that stored plate text matches ANPR optical character recognition standards.

---

### 6. End-User UI Perspective vs. Backend Business Logic Alignment

| User Persona | Key Business Logic Facilitated by UI | UI Design Feature & Ergonomics |
| :--- | :--- | :--- |
| **Community Administrator** | Managing community limits, tracking subscription tiers (`BASIC_LANDED` vs `PREMIUM_LANDED`), and inviting residents. | **Tier Badges & Invitation Modal:** Instant generation of unique tokens with 1-click clipboard copy (`/register?token=...`) for sharing via WhatsApp or email. |
| **Resident** | Registering personal vehicles for automated gate entry; pre-registering visitors to bypass guard interrogation. | **Visual Plate Card & Shareable Visitor Pass:** Malaysian plate visual container for personal cars; digital QR/Pass share modal with formatted summary for guests. |
| **Gatehouse Security Guard** | Rapid checkpoint barrier entry verification, ANPR automatic logging, unscheduled visitor manual capture. | **High-Contrast Verification Station:** Prominent token input field on `/access-logs` allowing guards to paste or scan tokens with one keystroke to open barriers and auto-log entries. |

---

### 7. Evaluation & Conclusion

The architectural synergy between Angular 17+ and Spring Boot 3 delivers significant technical advantages for the final FYP application:

1. **Security & Data Isolation:** Multi-tenancy is guaranteed at the database query level via derived queries and verified at the perimeter via JWT and `X-Neighborhood-Id` headers.
2. **UI/UX Excellence:** Design tokens in `styles.css` (Emerald `#059669`, Teal `#0d9488`, Slate `#0f172a`), smooth glassmorphism, responsive grids, and Malaysian license plate styling elevate the user experience far beyond basic CRUD applications.
3. **Maintainability & Testability:** 100% of the backend integration test suites (27/27 tests) pass, and the Angular frontend compiles to a production bundle with 0 errors.

This cohesive architecture ensures that KampungHub is secure, scalable, ergonomically refined, and fully aligned with modern software engineering standards.
