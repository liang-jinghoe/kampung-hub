# KampungHub - Project Requirements Mapping

This document lists the Front-End (Angular) and Back-End (Spring Boot) requirements and links them directly to their technical specifications and source code implementations.

---

## 🎨 Front-End Angular Requirements

Since the front-end Angular codebase is not yet initialized, these requirements are mapped to their detailed implementation designs and code specifications in the Front-End setup document.

1. **Dynamic page changes via interpolation and property binding** with appropriate template expressions.
   * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 1](setup-frontend.md#L52)
2. **Dynamic page changes via class and style binding** and/or `NgClass` and `NgStyle` together with basic CSS style rules.
   * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 2](setup-frontend.md#L63)
3. **Event binding** to respond to user events on the web page.
   * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 3](setup-frontend.md#L78)
4. **Component hierarchy** consisting of root, child, and grand-child components.
   * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 4 & 5](setup-frontend.md#L86)
5. **Data transfer between components** in the hierarchy using `@Input` and `@Output`.
   * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 5 (Inputs/Outputs)](setup-frontend.md#L100)
6. **Conditional dynamic page changes** using `@if`/`@else` or `@switch`/`@case` control flow blocks.
   * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 6](setup-frontend.md#L159)
7. **Rendering of multiple elements/child components** using `@for` with loop contextual variables (`$index`, `$count`, `$first`).
   * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 7](setup-frontend.md#L195)
8. **Reactive forms with grouped form control elements** to accept user input.
   * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 8](setup-frontend.md#L228)
9. **Form control validation** and display of validation error messages.
   * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 9](setup-frontend.md#L250)
10. **HTTPClient requests & RxJS Async network interaction** using `HttpClient`, `Observable`, and `Subscription`.
    * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 10](setup-frontend.md#L323)
11. **Route matching & routing configuration** (redirecting paths, wildcard 404 routes).
    * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 11-14 (Router)](setup-frontend.md#L372)
12. **Route and query parameter transmission** between routed components.
    * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 12 (Params)](setup-frontend.md#L416)
13. **Nested / child routes** mapping.
    * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 13 (Child Routes)](setup-frontend.md#L392)
14. **Programmatic navigation** in component classes.
    * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 14](setup-frontend.md#L416)
15. **Code explanations & session context switching** comments.
    * 🔗 Specification & Examples: [setup-frontend.md:FE-Req 15](setup-frontend.md#L454)

---

## ⚙️ Backend Spring Requirements

These requirements are mapped directly to their implementations inside the Spring Boot backend codebase directory:

1. **REST API Endpoint Design Specification** matching best practices with path/query parameters.
   * 🔗 API Contract Docs: [api-contract.md](api-contract.md)
   * 🔗 Specification Details: [setup-backend.md:BE-Req 1](setup-backend.md#L80)
2. **Spring Boot starter annotations & configurations** (`@RestController`, `@RequestMapping`, `@GetMapping`, Lombok, properties).
   * 🔗 Main Entry Application: [KampungHubBackendApplication.java](../kampung-hub-backend/src/main/java/com/kampung/security/KampungHubBackendApplication.java)
   * 🔗 Properties & Environment Configuration: [application.properties](../kampung-hub-backend/src/main/resources/application.properties)
3. **Database Domain Entities** mapping business classes to tables using Hibernate/Spring Data JPA:
   * 🔗 [Neighborhood.java](../kampung-hub-backend/src/main/java/com/kampung/security/entity/Neighborhood.java)
   * 🔗 [User.java](../kampung-hub-backend/src/main/java/com/kampung/security/entity/User.java)
   * 🔗 [Membership.java](../kampung-hub-backend/src/main/java/com/kampung/security/entity/Membership.java)
   * 🔗 [ResidentVehicle.java](../kampung-hub-backend/src/main/java/com/kampung/security/entity/ResidentVehicle.java)
   * 🔗 [VisitorPass.java](../kampung-hub-backend/src/main/java/com/kampung/security/entity/VisitorPass.java)
   * 🔗 [AccessLog.java](../kampung-hub-backend/src/main/java/com/kampung/security/entity/AccessLog.java)
4. **Basic CRUD operation repositories** using Spring Data `JpaRepository`:
   * 🔗 [NeighborhoodRepository.java](../kampung-hub-backend/src/main/java/com/kampung/security/repository/NeighborhoodRepository.java)
   * 🔗 [UserRepository.java](../kampung-hub-backend/src/main/java/com/kampung/security/repository/UserRepository.java)
   * 🔗 [MembershipRepository.java](../kampung-hub-backend/src/main/java/com/kampung/security/repository/MembershipRepository.java)
   * 🔗 [ResidentVehicleRepository.java](../kampung-hub-backend/src/main/java/com/kampung/security/repository/ResidentVehicleRepository.java)
   * 🔗 [VisitorPassRepository.java](../kampung-hub-backend/src/main/java/com/kampung/security/repository/VisitorPassRepository.java)
   * 🔗 [AccessLogRepository.java](../kampung-hub-backend/src/main/java/com/kampung/security/repository/AccessLogRepository.java)
5. **Derived, custom native SQL, and JPQL queries** implemented in repositories:
   * 🔗 [ResidentVehicleRepository.java (L12-L25)](../kampung-hub-backend/src/main/java/com/kampung/security/repository/ResidentVehicleRepository.java#L12-L25) (derived, JPQL and native queries)
   * 🔗 [MembershipRepository.java (L10-L12)](../kampung-hub-backend/src/main/java/com/kampung/security/repository/MembershipRepository.java#L10-L12) (derived queries)
   * 🔗 [UserRepository.java (L10)](../kampung-hub-backend/src/main/java/com/kampung/security/repository/UserRepository.java#L10) (derived query)
6. **Centralized Exception Handling & custom error mapping**:
   * 🔗 Specification & Error DTO Details: [setup-backend.md:BE-Req 6](setup-backend.md#L669)
7. **Short codebase comments explaining implemented requirements**:
   * 🔗 Present inside source files (e.g., entity descriptions and repository annotations).
