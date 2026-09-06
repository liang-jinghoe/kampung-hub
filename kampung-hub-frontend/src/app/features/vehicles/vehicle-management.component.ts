import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { VehicleService } from '../../core/services/vehicle.service';
import { AuthService } from '../../core/services/auth.service';
import { ResidentVehicle, CreateVehicleDto, UpdateVehicleDto } from '../../core/models/vehicle.model';
import { MembershipContext } from '../../core/models/membership.model';
import { VehicleListComponent } from './components/vehicle-list/vehicle-list.component';
import { VehicleFormComponent } from './components/vehicle-form/vehicle-form.component';

// FE-Req-1, 2, 3: Parent Container in 3-tier hierarchy orchestrating vehicle state and CRUD
@Component({
  selector: 'app-vehicle-management',
  standalone: true,
  imports: [CommonModule, VehicleListComponent, VehicleFormComponent],
  templateUrl: './vehicle-management.component.html',
  styleUrls: ['./vehicle-management.component.css']
})
export class VehicleManagementComponent implements OnInit, OnDestroy {
  vehicles: ResidentVehicle[] = [];
  isLoading = true;
  activeMembership: MembershipContext | null = null;
  
  showFormModal = false;
  selectedVehicleForEdit: ResidentVehicle | null = null;

  toast: { message: string; type: 'success' | 'error' } | null = null;
  private toastTimeout: any;
  private sub = new Subscription();

  constructor(
    private vehicleService: VehicleService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.sub.add(
      this.authService.session$.subscribe((session) => {
        this.activeMembership = session?.activeMembership || null;
        if (this.activeMembership?.neighborhoodId) {
          this.loadVehicles();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
  }

  loadVehicles(): void {
    if (!this.activeMembership) return;

    this.isLoading = true;
    const neighborhoodId = this.activeMembership.neighborhoodId;
    
    // If resident, can filter by their ownerMembershipId or see community
    const ownerMembershipId = this.isResidentOnly() ? this.activeMembership.membershipId : undefined;

    this.vehicleService.getVehicles(neighborhoodId, ownerMembershipId).subscribe({
      next: (data) => {
        this.vehicles = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast('Failed to load vehicles: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  openCreateModal(): void {
    this.selectedVehicleForEdit = null;
    this.showFormModal = true;
  }

  openEditModal(vehicle: ResidentVehicle): void {
    this.selectedVehicleForEdit = vehicle;
    this.showFormModal = true;
  }

  closeModal(): void {
    this.showFormModal = false;
    this.selectedVehicleForEdit = null;
  }

  handleFormSubmit(payload: any): void {
    if (this.selectedVehicleForEdit) {
      // Update existing vehicle
      const updateDto: UpdateVehicleDto = {
        model: payload.model,
        color: payload.color,
        zoneMask: payload.zoneMask,
        status: payload.status || 'ACTIVE'
      };

      this.vehicleService.updateVehicle(this.selectedVehicleForEdit.vehicleId, updateDto).subscribe({
        next: (updated) => {
          this.showToast(`Vehicle ${updated.plateText} updated successfully.`, 'success');
          this.closeModal();
          this.loadVehicles();
        },
        error: (err) => {
          this.showToast('Failed to update vehicle: ' + (err.error?.message || 'Server error'), 'error');
        }
      });
    } else {
      // Create new whitelist entry
      const createDto: CreateVehicleDto = {
        plateText: payload.plateText,
        model: payload.model,
        color: payload.color,
        unitNumber: payload.unitNumber || this.activeMembership?.unitNumber,
        zoneMask: payload.zoneMask || 'ALL_ZONES',
        neighborhoodId: this.activeMembership?.neighborhoodId,
        ownerMembershipId: this.activeMembership?.membershipId
      };

      this.vehicleService.createVehicle(createDto).subscribe({
        next: (created) => {
          this.showToast(`Vehicle ${created.plateText} added to whitelist!`, 'success');
          this.closeModal();
          this.loadVehicles();
        },
        error: (err) => {
          this.showToast('Failed to register vehicle: ' + (err.error?.message || 'Plate may already be registered.'), 'error');
        }
      });
    }
  }

  handleToggleStatus(vehicle: ResidentVehicle): void {
    const nextStatus = vehicle.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    const updateDto: UpdateVehicleDto = {
      model: vehicle.model,
      color: vehicle.color,
      zoneMask: vehicle.zoneMask,
      status: nextStatus
    };

    this.vehicleService.updateVehicle(vehicle.vehicleId, updateDto).subscribe({
      next: (updated) => {
        this.showToast(`Vehicle ${updated.plateText} status changed to ${nextStatus}.`, 'success');
        this.loadVehicles();
      },
      error: (err) => {
        this.showToast('Failed to change status: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  handleDelete(vehicle: ResidentVehicle): void {
    if (!confirm(`Are you sure you want to remove vehicle ${vehicle.plateText} from the whitelist?`)) {
      return;
    }

    this.vehicleService.deleteVehicle(vehicle.vehicleId).subscribe({
      next: () => {
        this.showToast(`Vehicle ${vehicle.plateText} removed from whitelist.`, 'success');
        this.loadVehicles();
      },
      error: (err) => {
        this.showToast('Failed to delete vehicle: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  showToast(message: string, type: 'success' | 'error'): void {
    this.toast = { message, type };
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
    this.toastTimeout = setTimeout(() => {
      this.toast = null;
    }, 4000);
  }

  get isAdmin(): boolean {
    return this.authService.hasRole('ADMIN') || this.authService.hasRole('COMMITTEE');
  }

  isResidentOnly(): boolean {
    return this.authService.hasRole('RESIDENT') && !this.isAdmin;
  }
}
