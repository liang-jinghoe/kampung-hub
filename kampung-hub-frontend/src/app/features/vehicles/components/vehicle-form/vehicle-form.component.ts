import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ResidentVehicle } from '../../../../core/models/vehicle.model';

// FE-Req-8 & FE-Req-9: Reactive Vehicle Form with Malaysian Plate Regex Validation & Dynamic Unit Handling
@Component({
  selector: 'app-vehicle-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './vehicle-form.component.html',
  styleUrls: ['./vehicle-form.component.css']
})
export class VehicleFormComponent implements OnInit, OnChanges {
  @Input() vehicleToEdit: ResidentVehicle | null = null;
  @Input() activeNeighborhoodId = '';
  @Input() activeUnitNumber = '';
  @Input() isAdmin = false;

  @Output() formSubmit = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  vehicleForm: FormGroup;
  // Malaysian plate regex: 1-3 Letters, optional space, 1-4 digits, optional letter suffix (e.g., WYY 1234 A, VAA 888, B 1234)
  plateRegex = /^[A-Z]{1,3}\s?[0-9]{1,4}\s?[A-Z]?$/i;

  constructor(private fb: FormBuilder) {
    this.vehicleForm = this.fb.group({
      plateText: ['', [Validators.required, Validators.pattern(this.plateRegex)]],
      model: ['', [Validators.required, Validators.minLength(2)]],
      color: ['', [Validators.required]],
      unitNumber: ['', [Validators.required]],
      zoneMask: ['ALL_ZONES'],
      status: ['ACTIVE']
    });
  }

  ngOnInit(): void {
    this.populateForm();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['vehicleToEdit'] || changes['activeUnitNumber']) {
      this.populateForm();
    }
  }

  private populateForm(): void {
    if (this.vehicleToEdit) {
      this.vehicleForm.patchValue({
        plateText: this.vehicleToEdit.plateText,
        model: this.vehicleToEdit.model,
        color: this.vehicleToEdit.color,
        unitNumber: this.vehicleToEdit.unitNumber,
        zoneMask: this.vehicleToEdit.zoneMask || 'ALL_ZONES',
        status: this.vehicleToEdit.status || 'ACTIVE'
      });
    } else {
      this.vehicleForm.reset({
        plateText: '',
        model: '',
        color: '',
        unitNumber: this.activeUnitNumber || '',
        zoneMask: 'ALL_ZONES',
        status: 'ACTIVE'
      });
    }
  }

  get f() {
    return this.vehicleForm.controls;
  }

  onPlateInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input) {
      const upper = input.value.toUpperCase();
      this.vehicleForm.get('plateText')?.setValue(upper, { emitEvent: false });
    }
  }

  onSubmit(): void {
    if (this.vehicleForm.invalid) {
      this.vehicleForm.markAllAsTouched();
      return;
    }

    const val = this.vehicleForm.value;
    const payload = {
      ...val,
      plateText: val.plateText.trim().toUpperCase(),
      neighborhoodId: this.activeNeighborhoodId
    };

    this.formSubmit.emit(payload);
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
