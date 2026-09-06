# KampungHub - Project Requirements Mapping

This document provides a comprehensive mapping of all **Front-End (Angular 17+)** and **Back-End (Spring Boot)** project requirements directly to their concrete source code implementations, components, services, and tests in the repository.

---

## 🎨 Front-End Angular Requirements

All front-end requirements are implemented in the standalone Angular 17+ architecture under [`kampung-hub-frontend/`](../kampung-hub-frontend):

### 1. Dynamic page changes via interpolation and property binding
* 🔗 **Source Implementations:**
  * [`VehicleCardComponent` Template](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-card/vehicle-card.component.html) (`{{ vehicle.plateText }}`, `{{ vehicle.unitNumber }}`, `[class.suspended]="!isActive"`)
  * [`NavbarComponent` Template](../kampung-hub-frontend/src/app/shared/components/navbar/navbar.component.html) (`{{ activeMembership?.neighborhoodName }}`, `[attr.aria-expanded]="isNeighborhoodDropdownOpen"`)
  * [`DashboardComponent` Template](../kampung-hub-frontend/src/app/features/dashboard/dashboard.component.html) (`{{ vehicleCount }}`, `{{ activePassCount }}`, `{{ recentLogsCount }}`)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 1](setup-frontend.md#L52)

### 2. Dynamic page changes via class and style binding (`NgClass` / `NgStyle`)
* 🔗 **Source Implementations:**
  * [`VehicleCardComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-card/vehicle-card.component.html) (`[ngClass]="isActive ? 'badge-success' : 'badge-danger'"`, `[style.background-color]="vehicle.color.toLowerCase()"`)
  * [`NavbarComponent`](../kampung-hub-frontend/src/app/shared/components/navbar/navbar.component.html) (`[ngClass]="{ 'badge-danger': hasRole('ADMIN'), 'badge-success': hasRole('RESIDENT'), ... }"`, `[class.rotate]="isNeighborhoodDropdownOpen"`)
  * [`VehicleListComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-list/vehicle-list.component.html) (`[class.active]="statusFilter === 'ACTIVE'"`)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 2](setup-frontend.md#L63)

### 3. Event binding to respond to user interactions
* 🔗 **Source Implementations:**
  * [`VehicleListComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-list/vehicle-list.component.html) (`(click)="setStatusFilter('ACTIVE')"`, `(click)="onCreate()"`, `[(ngModel)]="searchQuery"`)
  * [`VehicleCardComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-card/vehicle-card.component.html) (`(click)="onEdit()"`, `(click)="onToggleStatus()"`, `(click)="onDelete()"`)
  * [`NavbarComponent`](../kampung-hub-frontend/src/app/shared/components/navbar/navbar.component.html) (`(click)="toggleNeighborhoodDropdown($event)"`, `(click)="switchNeighborhood(m)"`)
  * [`AccessLogListComponent`](../kampung-hub-frontend/src/app/features/access-logs/access-log-list.component.html) (`(keyup.enter)="onDirectCheckIn()"`, `(click)="onDirectCheckIn()"`)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 3](setup-frontend.md#L78)

### 4. Component hierarchy (Root -> Child -> Grandchild)
* 🔗 **3-Tier Architecture:**
  * **Parent Container:** [`VehicleManagementComponent`](../kampung-hub-frontend/src/app/features/vehicles/vehicle-management.component.ts) (orchestrates state, modal triggers, context subscriptions)
  * **Child Component:** [`VehicleListComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-list/vehicle-list.component.ts) (handles search, sorting, status tabs)
  * **Grandchild Component:** [`VehicleCardComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-card/vehicle-card.component.ts) (renders individual Malaysian plate visual card and actions)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 4 & 5](setup-frontend.md#L86)

### 5. Data transfer between components using `@Input` and `@Output`
* 🔗 **Source Implementations:**
  * [`VehicleCardComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-card/vehicle-card.component.ts) (`@Input() vehicle`, `@Input() canManage`, `@Output() edit`, `@Output() delete`, `@Output() toggleStatus`)
  * [`VehicleListComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-list/vehicle-list.component.ts) (`@Input() vehicles`, `@Input() isLoading`, `@Output() createClick`, `@Output() editVehicle`, `@Output() deleteVehicle`, `@Output() toggleStatus`)
  * [`VehicleFormComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-form/vehicle-form.component.ts) (`@Input() vehicleToEdit`, `@Input() activeNeighborhoodId`, `@Output() formSubmit`, `@Output() cancel`)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 5](setup-frontend.md#L100)

### 6. Conditional dynamic page changes (`*ngIf` / `@if`)
* 🔗 **Source Implementations:**
  * [`NavbarComponent`](../kampung-hub-frontend/src/app/shared/components/navbar/navbar.component.html) (`*ngIf="session"`, `*ngIf="isNeighborhoodDropdownOpen"`, `*ngIf="isAdminOrCommittee()"`)
  * [`DashboardComponent`](../kampung-hub-frontend/src/app/features/dashboard/dashboard.component.html) (`*ngIf="isAdminOrCommittee()"`, `*ngIf="isResident()"`, `*ngIf="isGuard()"`)
  * [`VehicleListComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-list/vehicle-list.component.html) (`*ngIf="isLoading"`, `*ngIf="!isLoading && filteredVehicles.length === 0"`, `*ngIf="!isLoading && filteredVehicles.length > 0"`)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 6](setup-frontend.md#L159)

### 7. Rendering multiple elements and child components (`*ngFor` / `@for`)
* 🔗 **Source Implementations:**
  * [`VehicleListComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-list/vehicle-list.component.html) (`*ngFor="let vehicle of filteredVehicles"`)
  * [`NavbarComponent`](../kampung-hub-frontend/src/app/shared/components/navbar/navbar.component.html) (`*ngFor="let m of availableMemberships"`)
  * [`MemberListComponent`](../kampung-hub-frontend/src/app/features/members/member-list.component.html) (`*ngFor="let member of filteredMembers"`)
  * [`AccessLogListComponent`](../kampung-hub-frontend/src/app/features/access-logs/access-log-list.component.html) (`*ngFor="let log of filteredLogs"`)
  * [`NeighborhoodListComponent`](../kampung-hub-frontend/src/app/features/neighborhoods/neighborhood-list/neighborhood-list.component.html) (`*ngFor="let n of filteredNeighborhoods"`)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 7](setup-frontend.md#L195)

### 8. Reactive forms with grouped form control elements
* 🔗 **Source Implementations:**
  * [`LoginComponent`](../kampung-hub-frontend/src/app/features/auth/login/login.component.ts) (`loginForm = fb.group({ email: ..., password: ... })`)
  * [`VehicleFormComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-form/vehicle-form.component.ts) (`vehicleForm = fb.group({ plateText: ..., model: ..., color: ..., unitNumber: ..., zoneMask: ... })`)
  * [`MemberRegisterComponent`](../kampung-hub-frontend/src/app/features/auth/register/member-register.component.ts) (`registerForm = fb.group({ fullName: ..., phoneNumber: ..., password: ..., confirmPassword: ... })`)
  * [`VisitorPassListComponent`](../kampung-hub-frontend/src/app/features/visitor-passes/visitor-pass-list.component.ts) (`createForm = fb.group({ visitorName: ..., visitorPlateText: ..., validFrom: ..., validUntil: ... })`)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 8](setup-frontend.md#L228)

### 9. Form control validation and error messaging
* 🔗 **Source Implementations:**
  * Malaysian license plate regex pattern in [`VehicleFormComponent`](../kampung-hub-frontend/src/app/features/vehicles/components/vehicle-form/vehicle-form.component.ts#L25) (`/^[A-Z]{1,3}\s?[0-9]{1,4}\s?[A-Z]?$/i`)
  * Custom password-matching group validator in [`MemberRegisterComponent`](../kampung-hub-frontend/src/app/features/auth/register/member-register.component.ts#L7-L11)
  * Email format, required and minLength validators in [`LoginComponent`](../kampung-hub-frontend/src/app/features/auth/login/login.component.ts)
  * Error alert blocks and field error visual feedback in HTML templates (`.form-error`, `.is-invalid`, `.alert-danger`)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 9](setup-frontend.md#L250)

### 10. HttpClient requests & RxJS Async network interaction
* 🔗 **Source Implementations:**
  * [`AuthService`](../kampung-hub-frontend/src/app/core/services/auth.service.ts) (`BehaviorSubject<AuthResponse | null>`, `session$`, `login()`, `switchContext()`)
  * [`auth.interceptor.ts`](../kampung-hub-frontend/src/app/core/interceptors/auth.interceptor.ts) (attaches `Authorization: Bearer <jwt>` and `X-Neighborhood-Id: <id>`)
  * [`VehicleService`](../kampung-hub-frontend/src/app/core/services/vehicle.service.ts) (CRUD operations returning `Observable<ResidentVehicle[]>`)
  * [`NeighborhoodService`](../kampung-hub-frontend/src/app/core/services/neighborhood.service.ts) (Community metadata, member invites, token onboarding)
  * [`VisitorPassService`](../kampung-hub-frontend/src/app/core/services/visitor-pass.service.ts) (Pass search, generation, and gate check-in)
  * [`AccessLogService`](../kampung-hub-frontend/src/app/core/services/access-log.service.ts) (Checkpoint barrier logs)
  * Subscription management and unsubscription lifecycle in [`VehicleManagementComponent`](../kampung-hub-frontend/src/app/features/vehicles/vehicle-management.component.ts), [`DashboardComponent`](../kampung-hub-frontend/src/app/features/dashboard/dashboard.component.ts), and [`NavbarComponent`](../kampung-hub-frontend/src/app/shared/components/navbar/navbar.component.ts)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 10](setup-frontend.md#L323)

### 11. Route matching, routing configuration & route guards
* 🔗 **Source Implementations:**
  * [`app.routes.ts`](../kampung-hub-frontend/src/app/app.routes.ts) (Configures all application routes, child paths, and 404 wildcard route `path: '**'`)
  * [`auth.guard.ts`](../kampung-hub-frontend/src/app/core/guards/auth.guard.ts) (`authGuard` protecting private routes, `guestGuard` redirecting logged-in users)
  * [`NotFoundComponent`](../kampung-hub-frontend/src/app/shared/components/not-found/not-found.component.ts) (404 Fallback view)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 11](setup-frontend.md#L372)

### 12. Route and query parameter transmission
* 🔗 **Source Implementations:**
  * Path Parameter (`/neighborhoods/:id`) reading in [`NeighborhoodDetailComponent`](../kampung-hub-frontend/src/app/features/neighborhoods/neighborhood-detail/neighborhood-detail.component.ts) via `ActivatedRoute.params`
  * Query Parameter (`?tier=BASIC_LANDED` / `?tier=PREMIUM_LANDED`) filtering in [`NeighborhoodListComponent`](../kampung-hub-frontend/src/app/features/neighborhoods/neighborhood-list/neighborhood-list.component.ts) via `ActivatedRoute.queryParams`
  * Query Parameter (`?token=...`) onboarding token extraction in [`MemberRegisterComponent`](../kampung-hub-frontend/src/app/features/auth/register/member-register.component.ts)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 12](setup-frontend.md#L416)

### 13. Nested / Child routes mapping
* 🔗 **Source Implementations:**
  * Route Definitions in [`app.routes.ts`](../kampung-hub-frontend/src/app/app.routes.ts) under `/neighborhoods/:id` with children `vehicles`, `passes`, `members`
  * Nested Router Outlet in [`NeighborhoodDetailComponent`](../kampung-hub-frontend/src/app/features/neighborhoods/neighborhood-detail/neighborhood-detail.component.html) (`<router-outlet></router-outlet>`)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 13](setup-frontend.md#L392)

### 14. Programmatic navigation
* 🔗 **Source Implementations:**
  * [`NeighborhoodListComponent`](../kampung-hub-frontend/src/app/features/neighborhoods/neighborhood-list/neighborhood-list.component.ts) (`this.router.navigate(['/neighborhoods', id])`, `this.router.navigate([], { queryParams: { tier } })`)
  * [`NavbarComponent`](../kampung-hub-frontend/src/app/shared/components/navbar/navbar.component.ts) (`this.router.navigate(['/login'])`, `this.router.navigate(['/dashboard'])`)
  * [`LoginComponent`](../kampung-hub-frontend/src/app/features/auth/login/login.component.ts) (`this.router.navigateByUrl(this.returnUrl)`)
  * [`auth.guard.ts`](../kampung-hub-frontend/src/app/core/guards/auth.guard.ts) (`this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } })`)
* 🔗 Specification Details: [setup-frontend.md:FE-Req 14](setup-frontend.md#L416)

### 15. Navbar component, multi-neighborhood context switching & in-code comments
* 🔗 **Source Implementations:**
  * [`NavbarComponent`](../kampung-hub-frontend/src/app/shared/components/navbar/navbar.component.ts) ([HTML](../kampung-hub-frontend/src/app/shared/components/navbar/navbar.component.html) / [CSS](../kampung-hub-frontend/src/app/shared/components/navbar/navbar.component.css))
  * Live context switching via `AuthService.switchContext(...)` updating active token, neighborhood headers, and active roles.
  * Explicit requirement tags and explanatory docstrings across all TypeScript components and services.
* 🔗 Specification Details: [setup-frontend.md:FE-Req 15](setup-frontend.md#L454)

---

## ⚙️ Backend Spring Boot Requirements

All back-end requirements are implemented under [`kampung-hub-backend/`](../kampung-hub-backend):

### 1. REST API Endpoint Design Specification
* 🔗 API Contract Documentation: [`api-contract.md`](api-contract.md)
* 🔗 Controllers:
  * [`AuthController.java`](../kampung-hub-backend/src/main/java/com/kampung/security/controller/AuthController.java) (`POST /api/v1/auth/login`, `POST /api/v1/auth/context`)
  * [`NeighborhoodController.java`](../kampung-hub-backend/src/main/java/com/kampung/security/controller/NeighborhoodController.java) (`GET /api/v1/neighborhoods`, `POST /api/v1/neighborhoods`, `GET /api/v1/neighborhoods/{id}`)
  * [`ResidentVehicleController.java`](../kampung-hub-backend/src/main/java/com/kampung/security/controller/ResidentVehicleController.java) (`GET /api/v1/vehicles`, `POST /api/v1/vehicles`, `PUT /api/v1/vehicles/{id}`, `DELETE /api/v1/vehicles/{id}`)
  * [`MembershipController.java`](../kampung-hub-backend/src/main/java/com/kampung/security/controller/MembershipController.java) (`GET /members`, `POST /direct`, `POST /invite`, `PUT /{id}`, `DELETE /{id}`)
  * [`MemberRegisterController.java`](../kampung-hub-backend/src/main/java/com/kampung/security/controller/MemberRegisterController.java) (`POST /api/v1/members/register?token=...`)
  * [`VisitorPassController.java`](../kampung-hub-backend/src/main/java/com/kampung/security/controller/VisitorPassController.java) (`GET /visitor-passes`, `POST /visitor-passes`, `PUT /{id}/check-in`)
  * [`AccessLogController.java`](../kampung-hub-backend/src/main/java/com/kampung/security/controller/AccessLogController.java) (`GET /access-logs`, `POST /access-logs`)
* 🔗 Specification Details: [setup-backend.md:BE-Req 1](setup-backend.md#L80)

### 2. Spring Boot Starter Annotations & Security Configurations
* 🔗 Main Application: [`KampungHubBackendApplication.java`](../kampung-hub-backend/src/main/java/com/kampung/security/KampungHubBackendApplication.java)
* 🔗 Security Filter Chain & CORS: [`SecurityConfig.java`](../kampung-hub-backend/src/main/java/com/kampung/security/config/SecurityConfig.java)
* 🔗 JWT Authentication Filter: [`JwtAuthenticationFilter.java`](../kampung-hub-backend/src/main/java/com/kampung/security/config/JwtAuthenticationFilter.java)
* 🔗 JWT Token Generator & Validator: [`JwtTokenProvider.java`](../kampung-hub-backend/src/main/java/com/kampung/security/config/JwtTokenProvider.java)
* 🔗 Properties Configuration: [`application.properties`](../kampung-hub-backend/src/main/resources/application.properties)
* 🔗 Specification Details: [setup-backend.md:BE-Req 2](setup-backend.md#L201)

### 3. Database Domain Entities & Seed Data
* 🔗 Hibernate/JPA Entities:
  * [`Neighborhood.java`](../kampung-hub-backend/src/main/java/com/kampung/security/entity/Neighborhood.java)
  * [`User.java`](../kampung-hub-backend/src/main/java/com/kampung/security/entity/User.java)
  * [`Membership.java`](../kampung-hub-backend/src/main/java/com/kampung/security/entity/Membership.java)
  * [`ResidentVehicle.java`](../kampung-hub-backend/src/main/java/com/kampung/security/entity/ResidentVehicle.java)
  * [`VisitorPass.java`](../kampung-hub-backend/src/main/java/com/kampung/security/entity/VisitorPass.java)
  * [`AccessLog.java`](../kampung-hub-backend/src/main/java/com/kampung/security/entity/AccessLog.java)
* 🔗 Preloaded Seed Data: [`data.sql`](../kampung-hub-backend/src/main/resources/data.sql)
* 🔗 Specification Details: [setup-backend.md:BE-Req 3](setup-backend.md#L309)

### 4. Basic CRUD Repositories (`JpaRepository`)
* 🔗 Repositories:
  * [`NeighborhoodRepository.java`](../kampung-hub-backend/src/main/java/com/kampung/security/repository/NeighborhoodRepository.java)
  * [`UserRepository.java`](../kampung-hub-backend/src/main/java/com/kampung/security/repository/UserRepository.java)
  * [`MembershipRepository.java`](../kampung-hub-backend/src/main/java/com/kampung/security/repository/MembershipRepository.java)
  * [`ResidentVehicleRepository.java`](../kampung-hub-backend/src/main/java/com/kampung/security/repository/ResidentVehicleRepository.java)
  * [`VisitorPassRepository.java`](../kampung-hub-backend/src/main/java/com/kampung/security/repository/VisitorPassRepository.java)
  * [`AccessLogRepository.java`](../kampung-hub-backend/src/main/java/com/kampung/security/repository/AccessLogRepository.java)
* 🔗 Specification Details: [setup-backend.md:BE-Req 4](setup-backend.md#L552)

### 5. Derived, Custom Native SQL, and JPQL Queries
* 🔗 Query Implementations:
  * Derived and custom queries in [`ResidentVehicleRepository.java`](../kampung-hub-backend/src/main/java/com/kampung/security/repository/ResidentVehicleRepository.java) (`findByPlateTextIgnoreCaseAndNeighborhoodId`, `findByOwnerMembershipId`, `findByStatus`)
  * Derived lookup queries in [`MembershipRepository.java`](../kampung-hub-backend/src/main/java/com/kampung/security/repository/MembershipRepository.java) (`findByUserId`, `findByNeighborhoodId`, `findByInvitationToken`)
  * Derived unique lookups in [`UserRepository.java`](../kampung-hub-backend/src/main/java/com/kampung/security/repository/UserRepository.java) (`findByEmail`, `existsByEmail`)
* 🔗 Specification Details: [setup-backend.md:BE-Req 5](setup-backend.md#L609)

### 6. Centralized Global Exception Handling
* 🔗 Central Controller Advice: [`GlobalExceptionHandler.java`](../kampung-hub-backend/src/main/java/com/kampung/security/exception/GlobalExceptionHandler.java)
* 🔗 Structured Error DTO: [`ApiErrorResponse.java`](../kampung-hub-backend/src/main/java/com/kampung/security/exception/ApiErrorResponse.java)
* 🔗 Handles: `BadCredentialsException` (401), `ResourceNotFoundException` (404), `AccessDeniedException` (403), `MethodArgumentNotValidException` (400), and generic fallbacks (500).
* 🔗 Specification Details: [setup-backend.md:BE-Req 6](setup-backend.md#L669)

### 7. Codebase Documentation Comments
* 🔗 Comprehensive Javadoc and requirement tags (`// BE-Req-X`, `// FE-Req-Y`) present across all entity models, repository queries, service layer rules, controller handlers, and Angular TypeScript modules.
* 🔗 Specification Details: [setup-backend.md:BE-Req 7](setup-backend.md#L738)

---

## 🧪 Automated Verification Suite

* **Backend Integration Test Suite (27/27 Tests Passing)**:
  * [`KampungHubBackendApplicationTests.java`](../kampung-hub-backend/src/test/java/com/kampung/security/KampungHubBackendApplicationTests.java)
  * [`AuthAndNeighborhoodControllerTests.java`](../kampung-hub-backend/src/test/java/com/kampung/security/AuthAndNeighborhoodControllerTests.java)
  * [`ResidentVehicleControllerTests.java`](../kampung-hub-backend/src/test/java/com/kampung/security/ResidentVehicleControllerTests.java)
  * [`MembershipAndPassControllerTests.java`](../kampung-hub-backend/src/test/java/com/kampung/security/MembershipAndPassControllerTests.java)
* **Frontend Compilation**:
  * Production bundle passes with **0 errors**: `ng build` $\rightarrow$ `dist/kampung-hub-frontend`.
