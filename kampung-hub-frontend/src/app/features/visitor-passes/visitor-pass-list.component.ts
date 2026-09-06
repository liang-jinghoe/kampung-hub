import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { VisitorPassService } from '../../core/services/visitor-pass.service';
import { AuthService } from '../../core/services/auth.service';
import { VisitorPass, CreateVisitorPassDto } from '../../core/models/visitor-pass.model';
import { MembershipContext } from '../../core/models/membership.model';

// FE-Req-1, 8, 10: Visitor Passes Management, Generation and Guard Check-in
@Component({
  selector: 'app-visitor-pass-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './visitor-pass-list.component.html',
  styleUrls: ['./visitor-pass-list.component.css']
})
export class VisitorPassListComponent implements OnInit, OnDestroy {
  passes: VisitorPass[] = [];
  isLoading = true;
  activeMembership: MembershipContext | null = null;
  searchQuery = '';
  statusFilter: 'ALL' | 'ACTIVE' | 'USED' | 'EXPIRED' = 'ALL';

  showCreateModal = false;
  createForm: FormGroup;
  selectedPassForShare: VisitorPass | null = null;

  toast: { message: string; type: 'success' | 'error' } | null = null;
  private toastTimeout: any;
  private sub = new Subscription();

  plateRegex = /^[A-Z]{1,3}\s?[0-9]{1,4}\s?[A-Z]?$/i;

  constructor(
    private passService: VisitorPassService,
    public authService: AuthService,
    private fb: FormBuilder,
    private route: ActivatedRoute
  ) {
    const today = new Date().toISOString().substring(0, 10);
    const tomorrow = new Date(Date.now() + 86400000).toISOString().substring(0, 10);

    this.createForm = this.fb.group({
      visitorName: ['', [Validators.required, Validators.minLength(2)]],
      visitorPlateText: ['', [Validators.required, Validators.pattern(this.plateRegex)]],
      unitNumber: ['', [Validators.required]],
      validFrom: [today + 'T00:00:00', [Validators.required]],
      validUntil: [tomorrow + 'T23:59:59', [Validators.required]]
    });
  }

  get targetNeighborhoodId(): string {
    const routeId = this.route.snapshot.params['id'] || this.route.parent?.snapshot.params['id'];
    return routeId || this.activeMembership?.neighborhoodId || '';
  }

  ngOnInit(): void {
    this.sub.add(
      this.authService.session$.subscribe((session) => {
        this.activeMembership = session?.activeMembership || null;
        if (this.targetNeighborhoodId) {
          this.loadPasses();
        }
      })
    );

    if (this.route.parent) {
      this.sub.add(
        this.route.parent.params.subscribe(() => {
          if (this.targetNeighborhoodId) {
            this.loadPasses();
          }
        })
      );
    }
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
  }

  loadPasses(): void {
    const neighborhoodId = this.targetNeighborhoodId;
    if (!neighborhoodId) return;

    this.isLoading = true;
    const isViewingOwnNeighborhood = this.activeMembership?.neighborhoodId === neighborhoodId;
    const unitParam = (this.isResidentOnly() && isViewingOwnNeighborhood) ? this.activeMembership?.unitNumber : undefined;
    const statusParam = this.statusFilter !== 'ALL' ? this.statusFilter : undefined;

    this.passService.searchVisitorPasses(
      neighborhoodId,
      unitParam,
      undefined,
      statusParam
    ).subscribe({
      next: (data) => {
        this.passes = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast('Failed to load visitor passes: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  get filteredPasses(): VisitorPass[] {
    return this.passes.filter((p) => {
      if (this.statusFilter !== 'ALL' && p.status !== this.statusFilter) return false;
      if (this.searchQuery.trim()) {
        const q = this.searchQuery.toLowerCase().trim();
        const matchName = (p.visitorName || '').toLowerCase().includes(q);
        const matchPlate = (p.visitorPlateText || '').toLowerCase().includes(q);
        const matchUnit = (p.unitNumber || '').toLowerCase().includes(q);
        const matchToken = (p.passToken || '').toLowerCase().includes(q);
        return matchName || matchPlate || matchUnit || matchToken;
      }
      return true;
    });
  }

  openCreateModal(): void {
    const today = new Date().toISOString().substring(0, 10);
    const tomorrow = new Date(Date.now() + 86400000).toISOString().substring(0, 10);

    this.createForm.reset({
      visitorName: '',
      visitorPlateText: '',
      unitNumber: this.activeMembership?.unitNumber || '',
      validFrom: today + 'T00:00:00',
      validUntil: tomorrow + 'T23:59:59'
    });
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  onPlateInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input) {
      const upper = input.value.toUpperCase();
      this.createForm.get('visitorPlateText')?.setValue(upper, { emitEvent: false });
    }
  }

  onCreateSubmit(): void {
    if (this.createForm.invalid || !this.targetNeighborhoodId) {
      this.createForm.markAllAsTouched();
      return;
    }

    const formVal = this.createForm.value;
    const payload: CreateVisitorPassDto = {
      visitorName: formVal.visitorName,
      visitorPlateText: formVal.visitorPlateText.trim().toUpperCase(),
      unitNumber: formVal.unitNumber || this.activeMembership?.unitNumber || '',
      neighborhoodId: this.targetNeighborhoodId,
      validFrom: formVal.validFrom,
      validUntil: formVal.validUntil
    };

    this.passService.createVisitorPass(payload).subscribe({
      next: (created) => {
        this.showToast(`Visitor pass created for ${created.visitorName}!`, 'success');
        this.closeCreateModal();
        this.loadPasses();
        this.openShareModal(created);
      },
      error: (err) => {
        this.showToast('Failed to create visitor pass: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  checkInPass(pass: VisitorPass): void {
    this.passService.checkInVisitorPass(pass.passId).subscribe({
      next: (updated) => {
        this.showToast(`Pass for ${updated.visitorName} checked in successfully!`, 'success');
        this.loadPasses();
      },
      error: (err) => {
        this.showToast('Failed to check in pass: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  openShareModal(pass: VisitorPass): void {
    this.selectedPassForShare = pass;
  }

  closeShareModal(): void {
    this.selectedPassForShare = null;
  }

  copyToken(token: string): void {
    navigator.clipboard.writeText(token).then(() => {
      this.showToast('Visitor pass token copied to clipboard!', 'success');
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

  get isResident(): boolean {
    return this.authService.hasRole('RESIDENT') || this.authService.hasRole('OWNER') || this.authService.hasRole('TENANT');
  }

  get isGuard(): boolean {
    return this.authService.hasRole('GUARD') && !this.isAdmin && !this.isResident;
  }

  get canGeneratePass(): boolean {
    return !this.isGuard && (this.isAdmin || this.isResident);
  }

  get isAdminOrGuard(): boolean {
    return this.isAdmin || this.authService.hasRole('GUARD');
  }

  isResidentOnly(): boolean {
    return this.isResident && !this.isAdminOrGuard;
  }
}
