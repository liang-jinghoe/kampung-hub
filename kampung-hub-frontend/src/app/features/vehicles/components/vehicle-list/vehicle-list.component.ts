import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ResidentVehicle } from '../../../../core/models/vehicle.model';
import { VehicleCardComponent } from '../vehicle-card/vehicle-card.component';

// FE-Req-1, 2, 3: Child component in 3-tier hierarchy managing vehicle filtering, search and grid display
@Component({
  selector: 'app-vehicle-list',
  standalone: true,
  imports: [CommonModule, FormsModule, VehicleCardComponent],
  templateUrl: './vehicle-list.component.html',
  styleUrls: ['./vehicle-list.component.css']
})
export class VehicleListComponent {
  @Input() vehicles: ResidentVehicle[] = [];
  @Input() isLoading = false;
  @Input() canManage = true;

  @Output() createClick = new EventEmitter<void>();
  @Output() editVehicle = new EventEmitter<ResidentVehicle>();
  @Output() deleteVehicle = new EventEmitter<ResidentVehicle>();
  @Output() toggleStatus = new EventEmitter<ResidentVehicle>();

  searchQuery = '';
  statusFilter: 'ALL' | 'ACTIVE' | 'SUSPENDED' = 'ALL';
  sortBy: 'plate' | 'unit' | 'model' = 'plate';

  get filteredVehicles(): ResidentVehicle[] {
    return this.vehicles
      .filter((v) => {
        // Status filter
        if (this.statusFilter !== 'ALL' && v.status !== this.statusFilter) {
          return false;
        }
        // Search query filter
        if (this.searchQuery.trim()) {
          const q = this.searchQuery.toLowerCase().trim();
          const matchPlate = v.plateText.toLowerCase().includes(q);
          const matchModel = v.model.toLowerCase().includes(q);
          const matchUnit = v.unitNumber.toLowerCase().includes(q);
          const matchColor = v.color.toLowerCase().includes(q);
          return matchPlate || matchModel || matchUnit || matchColor;
        }
        return true;
      })
      .sort((a, b) => {
        if (this.sortBy === 'plate') return a.plateText.localeCompare(b.plateText);
        if (this.sortBy === 'unit') return a.unitNumber.localeCompare(b.unitNumber);
        if (this.sortBy === 'model') return a.model.localeCompare(b.model);
        return 0;
      });
  }

  setStatusFilter(filter: 'ALL' | 'ACTIVE' | 'SUSPENDED'): void {
    this.statusFilter = filter;
  }

  onCreate(): void {
    this.createClick.emit();
  }

  onEdit(vehicle: ResidentVehicle): void {
    this.editVehicle.emit(vehicle);
  }

  onDelete(vehicle: ResidentVehicle): void {
    this.deleteVehicle.emit(vehicle);
  }

  onToggleStatus(vehicle: ResidentVehicle): void {
    this.toggleStatus.emit(vehicle);
  }
}
