import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { VehicleService } from '../../core/services/vehicle.service';
import { VisitorPassService } from '../../core/services/visitor-pass.service';
import { AccessLogService } from '../../core/services/access-log.service';
import { NeighborhoodService } from '../../core/services/neighborhood.service';
import { MembershipContext } from '../../core/models/membership.model';
import { Neighborhood } from '../../core/models/neighborhood.model';
import { AuthResponse } from '../../core/models/auth.model';
import { ResidentVehicle } from '../../core/models/vehicle.model';
import { VisitorPass } from '../../core/models/visitor-pass.model';
import { AccessLog } from '../../core/models/access-log.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  activeMembership: MembershipContext | null = null;
  activeNeighborhood: Neighborhood | null = null;
  
  vehicleCount = 0;
  activePassCount = 0;
  recentLogsCount = 0;
  isLoading = true;

  private sub = new Subscription();

  constructor(
    public authService: AuthService,
    private vehicleService: VehicleService,
    private passService: VisitorPassService,
    private logService: AccessLogService,
    private neighborhoodService: NeighborhoodService
  ) {}

  ngOnInit(): void {
    this.sub.add(
      this.authService.session$.subscribe((session: AuthResponse | null) => {
        this.activeMembership = session?.activeMembership || null;
        if (this.activeMembership?.neighborhoodId) {
          this.loadDashboardData(this.activeMembership.neighborhoodId);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  loadDashboardData(neighborhoodId: string): void {
    this.isLoading = true;

    // Load active neighborhood details
    this.neighborhoodService.getNeighborhoodById(neighborhoodId).subscribe({
      next: (n: Neighborhood) => (this.activeNeighborhood = n),
      error: () => {}
    });

    // Load vehicles
    this.vehicleService.getVehicles().subscribe({
      next: (vehicles: ResidentVehicle[]) => (this.vehicleCount = vehicles.length),
      error: () => (this.vehicleCount = 0)
    });

    // Load passes
    this.passService.searchVisitorPasses(neighborhoodId).subscribe({
      next: (passes: VisitorPass[]) => {
        this.activePassCount = passes.filter((p: VisitorPass) => p.status === 'ACTIVE').length;
      },
      error: () => (this.activePassCount = 0)
    });

    // Load access logs
    this.logService.getAccessLogs(neighborhoodId, 0, 50).subscribe({
      next: (logs: AccessLog[]) => (this.recentLogsCount = logs.length),
      error: () => (this.recentLogsCount = 0),
      complete: () => (this.isLoading = false)
    });
  }

  hasRole(role: string): boolean {
    return this.authService.hasRole(role);
  }

  isAdminOrCommittee(): boolean {
    return this.hasRole('ADMIN') || this.hasRole('COMMITTEE');
  }

  isResident(): boolean {
    return this.hasRole('RESIDENT');
  }

  isGuard(): boolean {
    return this.hasRole('GUARD');
  }
}
