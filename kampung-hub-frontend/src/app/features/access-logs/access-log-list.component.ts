import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AccessLogService } from '../../core/services/access-log.service';
import { VisitorPassService } from '../../core/services/visitor-pass.service';
import { AuthService } from '../../core/services/auth.service';
import { AccessLog, CreateAccessLogDto } from '../../core/models/access-log.model';
import { MembershipContext } from '../../core/models/membership.model';

// FE-Req-1, 10: Gatehouse Checkpoint Control Station & Access Logs Management
@Component({
  selector: 'app-access-log-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './access-log-list.component.html',
  styleUrls: ['./access-log-list.component.css']
})
export class AccessLogListComponent implements OnInit, OnDestroy {
  logs: AccessLog[] = [];
  isLoading = true;
  activeMembership: MembershipContext | null = null;
  searchQuery = '';
  typeFilter: 'ALL' | 'ENTRY' | 'EXIT' = 'ALL';

  // Manual checkpoint log form
  manualLogForm: FormGroup;
  showManualModal = false;

  // Direct token check-in form
  tokenCheckInInput = '';
  isCheckingIn = false;

  toast: { message: string; type: 'success' | 'error' } | null = null;
  private toastTimeout: any;
  private sub = new Subscription();

  plateRegex = /^[A-Z]{1,3}\s?[0-9]{1,4}\s?[A-Z]?$/i;

  constructor(
    private logService: AccessLogService,
    private passService: VisitorPassService,
    public authService: AuthService,
    private fb: FormBuilder
  ) {
    this.manualLogForm = this.fb.group({
      plateText: ['', [Validators.required, Validators.pattern(this.plateRegex)]],
      accessType: ['ENTRY', [Validators.required]],
      visitorPassId: [''],
      membershipId: ['']
    });
  }

  ngOnInit(): void {
    this.sub.add(
      this.authService.session$.subscribe((session) => {
        this.activeMembership = session?.activeMembership || null;
        if (this.activeMembership?.neighborhoodId) {
          this.loadLogs();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
  }

  loadLogs(): void {
    if (!this.activeMembership?.neighborhoodId) return;

    this.isLoading = true;
    this.logService.getAccessLogs(this.activeMembership.neighborhoodId, 0, 100).subscribe({
      next: (data) => {
        this.logs = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast('Failed to load checkpoint logs: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  get filteredLogs(): AccessLog[] {
    return this.logs.filter((l) => {
      if (this.typeFilter !== 'ALL' && l.accessType !== this.typeFilter) return false;
      if (this.searchQuery.trim()) {
        const q = this.searchQuery.toLowerCase().trim();
        const matchPlate = (l.plateText || '').toLowerCase().includes(q);
        const matchGuard = (l.verifiedByGuardId || '').toLowerCase().includes(q);
        const matchPass = (l.visitorPassId || '').toLowerCase().includes(q);
        return matchPlate || matchGuard || matchPass;
      }
      return true;
    });
  }

  onDirectCheckIn(): void {
    const token = this.tokenCheckInInput.trim();
    if (!token || !this.activeMembership) return;

    this.isCheckingIn = true;
    // Search pass by token
    this.passService.searchVisitorPasses(this.activeMembership.neighborhoodId, undefined, token).subscribe({
      next: (passes) => {
        if (passes.length === 0) {
          this.isCheckingIn = false;
          this.showToast('No active visitor pass found with token: ' + token, 'error');
          return;
        }

        const targetPass = passes[0];
        if (targetPass.status !== 'ACTIVE') {
          this.isCheckingIn = false;
          this.showToast(`Pass for ${targetPass.visitorName} is already ${targetPass.status}.`, 'error');
          return;
        }

        this.passService.checkInVisitorPass(targetPass.passId).subscribe({
          next: (checkedIn) => {
            this.isCheckingIn = false;
            this.tokenCheckInInput = '';
            this.showToast(`Gate open! Pass for ${checkedIn.visitorName} (${checkedIn.visitorPlateText}) verified.`, 'success');
            this.loadLogs();
          },
          error: (err) => {
            this.isCheckingIn = false;
            this.showToast('Check-in error: ' + (err.error?.message || 'Server error'), 'error');
          }
        });
      },
      error: (err) => {
        this.isCheckingIn = false;
        this.showToast('Error searching pass: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  openManualModal(): void {
    this.manualLogForm.reset({
      plateText: '',
      accessType: 'ENTRY',
      visitorPassId: '',
      membershipId: ''
    });
    this.showManualModal = true;
  }

  closeManualModal(): void {
    this.showManualModal = false;
  }

  onManualPlateInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input) {
      const upper = input.value.toUpperCase();
      this.manualLogForm.get('plateText')?.setValue(upper, { emitEvent: false });
    }
  }

  onManualSubmit(): void {
    if (this.manualLogForm.invalid || !this.activeMembership) {
      this.manualLogForm.markAllAsTouched();
      return;
    }

    const formVal = this.manualLogForm.value;
    const payload: CreateAccessLogDto = {
      plateText: formVal.plateText.trim().toUpperCase(),
      accessType: formVal.accessType,
      visitorPassId: formVal.visitorPassId || undefined,
      membershipId: formVal.membershipId || undefined,
      neighborhoodId: this.activeMembership.neighborhoodId
    };

    this.logService.createAccessLog(payload).subscribe({
      next: (logged) => {
        this.showToast(`Access ${logged.accessType} log recorded for ${logged.plateText}!`, 'success');
        this.closeManualModal();
        this.loadLogs();
      },
      error: (err) => {
        this.showToast('Failed to record access log: ' + (err.error?.message || 'Server error'), 'error');
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

  get isGuardOrAdmin(): boolean {
    return this.authService.hasRole('GUARD') || this.authService.hasRole('ADMIN') || this.authService.hasRole('COMMITTEE');
  }
}
