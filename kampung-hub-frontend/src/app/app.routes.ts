import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { MemberRegisterComponent } from './features/auth/register/member-register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { VehicleManagementComponent } from './features/vehicles/vehicle-management.component';
import { NeighborhoodListComponent } from './features/neighborhoods/neighborhood-list/neighborhood-list.component';
import { NeighborhoodDetailComponent } from './features/neighborhoods/neighborhood-detail/neighborhood-detail.component';
import { MemberListComponent } from './features/members/member-list.component';
import { NotFoundComponent } from './shared/components/not-found/not-found.component';
import { authGuard, guestGuard } from './core/guards/auth.guard';

// FE-Req-11, 12, 13, 14: Complete routing configuration with nested child routes and guards
export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [guestGuard]
  },
  {
    path: 'register',
    component: MemberRegisterComponent
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'vehicles',
    component: VehicleManagementComponent,
    canActivate: [authGuard]
  },
  {
    path: 'neighborhoods',
    component: NeighborhoodListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'neighborhoods/:id',
    component: NeighborhoodDetailComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'vehicles',
        pathMatch: 'full'
      },
      {
        path: 'vehicles',
        component: VehicleManagementComponent
      },
      {
        path: 'members',
        component: MemberListComponent
      }
    ]
  },
  {
    path: 'members',
    component: MemberListComponent,
    canActivate: [authGuard]
  },
  {
    path: '404',
    component: NotFoundComponent
  },
  {
    path: '**',
    component: NotFoundComponent
  }
];
