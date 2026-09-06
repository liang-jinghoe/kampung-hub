import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResidentVehicle } from '../../../../core/models/vehicle.model';

// FE-Req-1, 2, 3: Grandchild component representing an individual vehicle card
@Component({
  selector: 'app-vehicle-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './vehicle-card.component.html',
  styleUrls: ['./vehicle-card.component.css']
})
export class VehicleCardComponent {
  @Input({ required: true }) vehicle!: ResidentVehicle;
  @Input() canManage = true;

  @Output() edit = new EventEmitter<ResidentVehicle>();
  @Output() delete = new EventEmitter<ResidentVehicle>();
  @Output() toggleStatus = new EventEmitter<ResidentVehicle>();

  onEdit(): void {
    this.edit.emit(this.vehicle);
  }

  onDelete(): void {
    this.delete.emit(this.vehicle);
  }

  onToggleStatus(): void {
    this.toggleStatus.emit(this.vehicle);
  }

  get isActive(): boolean {
    return this.vehicle.status === 'ACTIVE';
  }
}
