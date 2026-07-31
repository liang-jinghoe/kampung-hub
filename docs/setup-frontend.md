# KampungHub Front-End Setup & Specification (Angular 17+)

This document outlines the front-end architecture, directory layout, and detailed requirement specifications for the **KampungHub Frontend Application**.

---

## 1.1 Project & Directory Structure
The front-end is structured into core, shared, and feature modules/components, leveraging Angular 17+ standalone components and modern control flow syntax (`@if`, `@for`, `@switch`).

```
kampung-hub-frontend/
├── src/
│   ├── app/
│   │   ├── core/
│   │   │   ├── models/            # DTOs: user.model.ts, membership.model.ts, vehicle.model.ts, visitor-pass.model.ts, access-log.model.ts
│   │   │   └── services/          # Services: auth.service.ts, neighborhood.service.ts, vehicle.service.ts, visitor-pass.service.ts, access-log.service.ts
│   │   ├── shared/
│   │   │   └── components/
│   │   │       └── navbar/        # Shared Header: dropdown selector for switching neighborhoods
│   │   ├── features/
│   │   │   ├── auth/              # Auth Screens: login/ and context-selector/
│   │   │   │   ├── login/
│   │   │   │   └── context-selector/
│   │   │   ├── neighborhoods/     # Admin Screen: Neighborhood Directory
│   │   │   │   ├── neighborhood-list/
│   │   │   │   └── neighborhood-detail/
│   │   │   ├── members/           # Admin/Resident Screen: Membership Management & Onboarding
│   │   │   │   ├── member-list/   # Admin Screen: View all registered memberships
│   │   │   │   ├── member-form/   # Admin Screen: Send invitation to new user
│   │   │   │   └── member-register/ # Public Screen: Register user & activate membership via token
│   │   │   ├── vehicles/          # Mixed Screen: Whitelist Management (Admin / Resident)
│   │   │   │   ├── vehicle-management/   # Container (Parent Component)
│   │   │   │   ├── vehicle-list/         # Child Component
│   │   │   │   ├── vehicle-card/         # Grandchild Component
│   │   │   │   ├── vehicle-form/         # Reactive Form Component
│   │   │   │   └── vehicle-owner-picker/ # Membership lookup/select control for owner assignment
│   │   │   ├── visitor-passes/    # Resident Screen: Pre-register Visitor Passes
│   │   │   │   ├── pass-list/
│   │   │   │   └── pass-create/
│   │   │   ├── resident-dashboard/# Resident Screen: Unit Overview & Visitor Passes List
│   │   │   └── guard-dashboard/   # Guard Screen: Access Control Verification & Logs
│   │   ├── app.component.ts       # Root Component
│   │   ├── app.routes.ts          # Angular Router Definition
│   │   └── app.config.ts          # Application Configuration (Providers, HttpClient, Router)
│   └── styles.css
```

---

## 1.2 Detailed Requirement Mapping & Code Implementation Rules

#### [FE-Req 1] Dynamic Page Changes via Interpolation & Property Binding
* **Implementation**: Use `{{ expression }}` for text binding and `[property]="expression"` for HTML attributes/DOM properties.
* **Example Code (`vehicle-card.component.html`)**:
    ```html
    <div class="vehicle-header">
      <h3>{{ vehicle.plateText }}</h3>
      <span class="badge">{{ vehicle.model }} ({{ vehicle.color }})</span>
    </div>
    <img [src]="vehicle.badgeImageUrl" [alt]="vehicle.model + ' badge image'" class="vehicle-badge-img" />
    ```

#### [FE-Req 2] Class/Style Binding, NgClass & NgStyle
* **Implementation**: Apply dynamic CSS classes and inline styles based on component state variables (e.g., status flags, active selection).
* **Example Code (`vehicle-card.component.html` & `.ts`)**:
    ```html
    <div class="status-indicator"
         [class.status-active]="vehicle.status === 'ACTIVE'"
         [class.status-suspended]="vehicle.status === 'SUSPENDED'"
         [ngStyle]="{ 'border-left': vehicle.status === 'ACTIVE' ? '6px solid #2e7d32' : '6px solid #c62828' }">
      Status: {{ vehicle.status }}
    </div>
    <button [ngClass]="{'btn-primary': isPrimary, 'btn-secondary': !isPrimary}">
      Select Vehicle
    </button>
    ```

#### [FE-Req 3] Event Binding for User Interaction
* **Implementation**: Listen to user events like `(click)`, `(input)`, `(submit)`, and `(change)`.
* **Example Code (`vehicle-card.component.html`)**:
    ```html
    <button (click)="onToggleStatus()" class="btn-action">Toggle Status</button>
    <button (click)="onDeleteVehicle($event)" class="btn-danger">Delete Vehicle</button>
    ```

#### [FE-Req 4 & 5] Component Hierarchy & `@Input()` / `@Output()` Data Transfer
* **Hierarchy**: `VehicleManagementComponent` (Root/Container) $\rightarrow$ `VehicleListComponent` (Child) $\rightarrow$ `VehicleCardComponent` (Grandchild).
* **Example Code**:
    * **Grandchild (`vehicle-card.component.ts`)**:
        ```typescript
        import { Component, Input, Output, EventEmitter } from '@angular/core';
        import { ResidentVehicle } from '../../../core/models/vehicle.model';

        @Component({
          selector: 'app-vehicle-card',
          standalone: true,
          templateUrl: './vehicle-card.component.html'
        })
        export class VehicleCardComponent {
          // FE-Req-5: Receiving data from parent component via @Input
          @Input({ required: true }) vehicle!: ResidentVehicle;

          // FE-Req-5: Emitting events back to parent component via @Output
          @Output() statusChanged = new EventEmitter<string>();
          @Output() vehicleDeleted = new EventEmitter<string>();

          onToggleStatus(): void {
            const newStatus = this.vehicle.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
            // FE-Req-5: Emit event payload to parent
            this.statusChanged.emit(newStatus);
          }

          onDeleteVehicle(event: Event): void {
            event.stopPropagation();
            this.vehicleDeleted.emit(this.vehicle.vehicleId);
          }
        }
        ```
    * **Child (`vehicle-list.component.ts`)**:
        ```typescript
        import { Component, Input, Output, EventEmitter } from '@angular/core';
        import { ResidentVehicle } from '../../../core/models/vehicle.model';
        import { VehicleCardComponent } from '../vehicle-card/vehicle-card.component';

        @Component({
          selector: 'app-vehicle-list',
          standalone: true,
          imports: [VehicleCardComponent],
          template: `
            <div class="vehicle-grid">
              @for (v of vehicles; track v.vehicleId) {
                <app-vehicle-card
                  [vehicle]="v"
                  (statusChanged)="handleStatusChange(v.vehicleId, $event)"
                  (vehicleDeleted)="handleDelete(v.vehicleId)">
                </app-vehicle-card>
              }
            </div>
          `
        })
        export class VehicleListComponent {
          // FE-Req-5: Receiving vehicle list from parent container
          @Input() vehicles: ResidentVehicle[] = [];

          // FE-Req-5: Relaying events up to parent container
          @Output() vehicleUpdate = new EventEmitter<{ id: string; status: string }>();
          @Output() vehicleRemove = new EventEmitter<string>();

          handleStatusChange(id: string, status: string): void {
            this.vehicleUpdate.emit({ id, status });
          }

          handleDelete(id: string): void {
            this.vehicleRemove.emit(id);
          }
        }
        ```

#### [FE-Req 6] Conditional Dynamic Rendering (`@if`, `@else`, `@switch`, `@case`)
* **Example Code (`vehicle-management.component.html`)**:
    ```html
    @if (isLoading) {
      <div class="spinner-container">
        <p>Loading neighborhood vehicle directory...</p>
      </div>
    } @else if (errorMessage) {
      <div class="alert alert-danger">
        <p>{{ errorMessage }}</p>
      </div>
    } @else {
      @if (vehicles.length > 0) {
        <app-vehicle-list
          [vehicles]="vehicles"
          (vehicleUpdate)="onUpdateVehicleStatus($event)"
          (vehicleRemove)="onRemoveVehicle($event)">
        </app-vehicle-list>
      } @else {
        <p class="empty-state">No whitelisted vehicles found for this property unit.</p>
      }
    }

    @switch (selectedTab) {
      @case ('WHITELIST') {
        <p>Showing active resident whitelist registry.</p>
      }
      @case ('VISITORS') {
        <p>Showing temporary pre-registered visitor passes.</p>
      }
      @default {
        <p>Select a tab to view operational security records.</p>
      }
    }
    ```

#### [FE-Req 7] Rendering Lists using `@for` with Contextual Variables (`$index`, `$count`, `$first`)
* **Example Code (`vehicle-table.component.html`)**:
    ```html
    <table class="data-table">
      <thead>
        <tr>
          <th>#</th>
          <th>Plate Number</th>
          <th>Model</th>
          <th>Color</th>
          <th>Zone</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        @for (item of vehicles; track item.vehicleId; let idx = $index; let total = $count; let isFirst =$first) {
          <tr [class.highlight-first]="isFirst">
            <td>{{ idx + 1 }} / {{ total }}</td>
            <td><strong>{{ item.plateText }}</strong></td>
            <td>{{ item.model }}</td>
            <td>{{ item.color }}</td>
            <td>{{ item.zoneMask }}</td>
            <td>{{ item.status }}</td>
          </tr>
        } @empty {
          <tr>
            <td colspan="6">No vehicle entries match the current filter criteria.</td>
          </tr>
        }
      </tbody>
    </table>
    ```

#### [FE-Req 8 & 9] Reactive Forms, Grouped Form Controls & Validation
* **Example Code (`vehicle-form.component.ts`)**:
    ```typescript
    import { Component, OnInit, Output, EventEmitter } from '@angular/core';
    import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

    @Component({
      selector: 'app-vehicle-form',
      standalone: true,
      imports: [ReactiveFormsModule],
      templateUrl: './vehicle-form.component.html'
    })
    export class VehicleFormComponent implements OnInit {
      // FE-Req-8: Reactive FormGroup containing grouped form controls
      vehicleForm!: FormGroup;

      @Output() formSubmitted = new EventEmitter<any>();

      constructor(private fb: FormBuilder) {}

      ngOnInit(): void {
        // FE-Req-8: Initialize grouped Reactive Form Controls with validators
        this.vehicleForm = this.fb.group({
          plateText: ['', [
            Validators.required,
            Validators.pattern('^[A-Z]{1,3}\\s?[0-9]{1,4}\\s?[A-Z]?$') // Malaysian plate pattern
          ]],
          vehicleDetails: this.fb.group({ // Grouped control subgroup
            model: ['', [Validators.required, Validators.minLength(2)]],
            color: ['', [Validators.required]]
          }),
          zoneMask: ['', [Validators.required]],
          unitNumber: ['', [Validators.required]],
          ownerMembershipId: ['', [Validators.required]]
        });
      }

      onSubmit(): void {
        if (this.vehicleForm.valid) {
          // FE-Req-8 & 9: Pass valid form payload to container
          this.formSubmitted.emit(this.vehicleForm.value);
          this.vehicleForm.reset();
        } else {
          // FE-Req-9: Mark all controls touched to force validation display
          this.vehicleForm.markAllAsTouched();
        }
      }

      // Helper getters for validation display in template
      get plateText() { return this.vehicleForm.get('plateText'); }
      get model() { return this.vehicleForm.get('vehicleDetails.model'); }
      get color() { return this.vehicleForm.get('vehicleDetails.color'); }
    }
    ```
* **Example Code (`vehicle-form.component.html`)**:
    ```html
    <form [formGroup]="vehicleForm" (ngSubmit)="onSubmit()" class="reactive-form">
      <div class="form-group">
        <label for="plateText">Plate Number</label>
        <input id="plateText" type="text" formControlName="plateText" placeholder="e.g. VHM8807" />
        @if (plateText?.invalid && (plateText?.touched || plateText?.dirty)) {
          <div class="error-msg">
            @if (plateText?.errors?.['required']) { <span>Plate number is required.</span> }
            @if (plateText?.errors?.['pattern']) { <span>Invalid Malaysian plate format (e.g. VHM8807 or W1234A).</span> }
          </div>
        }
      </div>

      <div formGroupName="vehicleDetails" class="form-subgroup">
        <div class="form-group">
          <label for="model">Vehicle Model</label>
          <input id="model" type="text" formControlName="model" placeholder="e.g. Myvi, Saga, Civic" />
          @if (model?.invalid && (model?.touched || model?.dirty)) {
            <div class="error-msg">
              @if (model?.errors?.['required']) { <span>Vehicle model is required.</span> }
              @if (model?.errors?.['minlength']) { <span>Model must be at least 2 characters.</span> }
            </div>
          }
        </div>

        <div class="form-group">
          <label for="color">Vehicle Color</label>
          <input id="color" type="text" formControlName="color" placeholder="e.g. WHITE, SILVER, BLACK" />
          @if (color?.invalid && (color?.touched || color?.dirty)) {
            <div class="error-msg">
              <span>Color selection is required.</span>
            </div>
          }
        </div>
      </div>

      <button type="submit" [disabled]="vehicleForm.invalid" class="btn-submit">Register Vehicle</button>
    </form>
    ```

#### [FE-Req 10] HTTPClient Requests & RxJS Async Interaction
* **Example Code (`vehicle.service.ts`)**:
    ```typescript
    import { Injectable } from '@angular/core';
    import { HttpClient, HttpParams } from '@angular/common/http';
    import { Observable, Subscription, catchError, throwError } from 'rxjs';
    import { NeighborhoodMember } from '../models/neighborhood-member.model';
    import { ResidentVehicle } from '../models/vehicle.model';

    @Injectable({ providedIn: 'root' })
    export class VehicleService {
      private readonly apiUrl = 'http://localhost:8080/api/v1/vehicles';

      // FE-Req-10: Injecting HttpClient library
      constructor(private http: HttpClient) {}

      // FE-Req-10: GET HTTP request returning an Observable array
      getVehicles(neighborhoodId: string, status?: string): Observable<ResidentVehicle[]> {
        let params = new HttpParams().set('neighborhoodId', neighborhoodId);
        if (status) {
          params = params.set('status', status);
        }
        return this.http.get<ResidentVehicle[]>(this.apiUrl, { params }).pipe(
          catchError((err) => throwError(() => new Error('Failed to fetch vehicles from backend.')))
        );
      }

      // FE-Req-10: POST HTTP request to register a new vehicle
      createVehicle(vehicleData: Partial<ResidentVehicle>): Observable<ResidentVehicle> {
        return this.http.post<ResidentVehicle>(this.apiUrl, vehicleData);
      }

      // FE-Req-10: GET HTTP request for neighborhood memberships used by vehicle ownership selection
      getMemberships(neighborhoodId: string): Observable<Membership[]> {
        return this.http.get<Membership[]>(`http://localhost:8080/api/v1/neighborhoods/${neighborhoodId}/members`);
      }

      // FE-Req-10: PUT HTTP request to update vehicle details
      updateVehicle(id: string, vehicleData: Partial<ResidentVehicle>): Observable<ResidentVehicle> {
        return this.http.put<ResidentVehicle>(`${this.apiUrl}/${id}`, vehicleData);
      }

      // FE-Req-10: DELETE HTTP request
      deleteVehicle(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
      }
    }
    ```

#### [FE-Req 11 - 14] Angular Routing, Query Params, Nested Child Routes & Programmatic Navigation
* **Route Definition (`app.routes.ts`)**:
    ```typescript
    import { Routes } from '@angular/router';
    import { NeighborhoodListComponent } from './features/neighborhoods/neighborhood-list/neighborhood-list.component';
    import { NeighborhoodDetailComponent } from './features/neighborhoods/neighborhood-detail/neighborhood-detail.component';
    import { VehicleManagementComponent } from './features/vehicles/vehicle-management/vehicle-management.component';
    import { VisitorPassListComponent } from './features/visitor-passes/pass-list/pass-list.component';
    import { ResidentDashboardComponent } from './features/resident-dashboard/resident-dashboard.component';
    import { GuardDashboardComponent } from './features/guard-dashboard/guard-dashboard.component';
    import { MemberRegisterComponent } from './features/members/member-register/member-register.component';
    import { NotFoundComponent } from './shared/not-found/not-found.component';

    export const routes: Routes = [
      // FE-Req-11: Route redirecting
      { path: '', redirectTo: 'neighborhoods', pathMatch: 'full' },

      // FE-Req-11: Standard path mapping (Admin screens)
      { path: 'neighborhoods', component: NeighborhoodListComponent },

      // FE-Req-12 & 13: Route with parameters and nested child routes (Admin/Resident screens)
      {
        path: 'neighborhoods/:id',
        component: NeighborhoodDetailComponent,
        children: [
          // FE-Req-13: Nested child routes
          { path: 'vehicles', component: VehicleManagementComponent },
          { path: 'visitor-passes', component: VisitorPassListComponent }
        ]
      },

      // Resident Portal routes
      { path: 'resident/dashboard', component: ResidentDashboardComponent },

      // Resident Invitation-based Onboarding / Registration route (Public)
      { path: 'members/register', component: MemberRegisterComponent },

      // Guardhouse / Security Gate routes
      { path: 'guard/dashboard', component: GuardDashboardComponent },

      // FE-Req-11: Wildcard route for 404 error handling
      { path: '**', component: NotFoundComponent }
    ];
    ```
* **Programmatic Navigation & Query Parameter Handling (`neighborhood-list.component.ts`)**:
    ```typescript
    import { Component, OnInit } from '@angular/core';
    import { Router, ActivatedRoute } from '@angular/router';

    @Component({
      selector: 'app-neighborhood-list',
      standalone: true,
      template: `
        <button (click)="navigateToNeighborhood('nh-usj4-uuid', 'PREMIUM')">
          View Taman USJ 4
        </button>
      `
    })
    export class NeighborhoodListComponent implements OnInit {
      // FE-Req-14: Injecting Router for programmatic navigation
      constructor(
        private router: Router,
        private route: ActivatedRoute
      ) {}

      ngOnInit(): void {
        // FE-Req-12: Reading query parameters from route
        this.route.queryParams.subscribe(params => {
          const filterTier = params['tier'];
          console.log('Filtered Tier query param:', filterTier);
        });
      }

      navigateToNeighborhood(id: string, tier: string): void {
        // FE-Req-12 & 14: Programmatic navigation with path parameters and query parameters
        this.router.navigate(['/neighborhoods', id, 'vehicles'], {
          queryParams: { tier: tier, activeTab: 'WHITELIST' }
        });
      }
    }
    ```

#### [FE-Req 15] User Login & Multi-Neighborhood Context Switching
* **Implementation**: Provide a shared navbar/header component that displays a dropdown of the user's registered memberships. When the user selects a neighborhood, show a loading spinner and request a context switch JWT token from the backend.
* **Example Code (`navbar.component.ts`)**:
    ```typescript
    import { Component, OnInit } from '@angular/core';
    import { Router } from '@angular/router';
    import { AuthService } from '../../core/services/auth.service';
    import { Membership } from '../../core/models/membership.model';
    import { NgFor, NgIf } from '@angular/common';

    @Component({
      selector: 'app-navbar',
      standalone: true,
      imports: [NgFor, NgIf],
      template: `
        <nav class="navbar">
          <span class="logo">KampungHub</span>
          <div class="user-context" *ngIf="userProfile">
            <span class="welcome-text">Welcome, {{ userProfile.fullName }}</span>
            
            <!-- Context Switch Dropdown -->
            <select [value]="activeMembershipId" (change)="onNeighborhoodChange($event)">
              <option *ngFor="let mem of memberships" [value]="mem.membershipId">
                {{ mem.neighborhoodName }} ({{ mem.unitNumber }})
              </option>
            </select>
          </div>
          
          <!-- Loading Overlay -->
          <div class="loading-overlay" *ngIf="isLoading">
            <span class="spinner">Switching neighborhood context...</span>
          </div>
        </nav>
      `
    })
    export class NavbarComponent implements OnInit {
      userProfile: any = null;
      memberships: Membership[] = [];
      activeMembershipId: string = '';
      isLoading = false;

      constructor(private authService: AuthService, private router: Router) {}

      ngOnInit(): void {
        this.userProfile = this.authService.getCurrentUser();
        this.memberships = this.authService.getAvailableMemberships();
        this.activeMembershipId = this.authService.getActiveMembershipId();
      }

      onNeighborhoodChange(event: Event): void {
        const selectEl = event.target as HTMLSelectElement;
        const targetMembershipId = selectEl.value;
        
        if (targetMembershipId !== this.activeMembershipId) {
          this.isLoading = true; // Show loading indicator

          // FE-Req-15: Trigger backend context selection and update JWT token
          this.authService.switchContext(targetMembershipId).subscribe({
            next: (newSession) => {
              this.activeMembershipId = newSession.activeMembership.membershipId;
              this.isLoading = false; // Hide loading indicator
              
              // Redirect to reload the dashboard for the new neighborhood context
              this.router.navigate(['/resident/dashboard']).then(() => {
                window.location.reload();
              });
            },
            error: (err) => {
              console.error('Failed to switch neighborhood context', err);
              this.isLoading = false;
            }
          });
        }
      }
    }
    ```

---

## 1.3 Front-End Build & Initialization Checklist

When setting up the front-end, follow this sequence:
1. **Initialize Angular 17+ App**
   * Create the project using: `npx -y @angular/cli@latest new kampung-hub-frontend --standalone --routing --style=css` (non-interactive mode).
2. **Create Core Layer Components**
   * Models: `user.model.ts`, `membership.model.ts`, `vehicle.model.ts`, `visitor-pass.model.ts`, `access-log.model.ts`.
   * Services: Write `auth.service.ts` for session handling, token management, and HTTP client requests [FE-Req 10, 15].
3. **Set Up Routing Configuration**
   * Configure `app.routes.ts` with routing targets and redirect, wildcard, nested child, and parameter patterns [FE-Req 11-14].
4. **Develop Feature Screens**
   * Build `NavbarComponent` with neighborhood selector dropdown and switching overlay indicator [FE-Req 15].
   * Build `VehicleManagementComponent` (Container/Parent) [FE-Req 4].
   * Build `VehicleListComponent` (Child) rendering lists and handle empty states via modern `@for` [FE-Req 5, 7].
   * Build `VehicleCardComponent` (Grandchild) with `@Input()`, `@Output()`, interpolation, class bindings, and event parameters [FE-Req 1-3].
   * Build `VehicleFormComponent` using `FormBuilder` to implement reactive validation [FE-Req 8, 9].
   * Build onboarding/registration screen `MemberRegisterComponent` to accept invite token and activate member.
5. **Verify CORS & Integration**
   * Configure proxy or set backend URL context and test HTTP requests.
