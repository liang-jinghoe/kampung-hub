import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { NeighborhoodService } from '../../core/services/neighborhood.service';
import { AuthService } from '../../core/services/auth.service';
import { Membership } from '../../core/models/membership.model';
import { MembershipContext } from '../../core/models/membership.model';

// FE-Req-8, 10: Member Directory & Invitation Token Onboarding Management
@Component({
  selector: 'app-member-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.css']
})
export class MemberListComponent implements OnInit, OnDestroy {
  members: Membership[] = [];
  isLoading = true;
  activeMembership: MembershipContext | null = null;
  searchQuery = '';
  statusFilter: 'ALL' | 'ACTIVE' | 'INVITED' = 'ALL';

  showInviteModal = false;
  inviteForm: FormGroup;
  
  generatedInviteUrl = '';
  showSuccessModal = false;

  toast: { message: string; type: 'success' | 'error' } | null = null;
  private toastTimeout: any;
  private sub = new Subscription();

  constructor(
    private neighborhoodService: NeighborhoodService,
    public authService: AuthService,
    private fb: FormBuilder,
    private route: ActivatedRoute
  ) {
    this.inviteForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      fullName: ['', [Validators.required]],
      unitNumber: ['', [Validators.required]],
      phoneNumber: [''],
      roles: ['RESIDENT', [Validators.required]]
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
          this.loadMembers();
        }
      })
    );

    if (this.route.parent) {
      this.sub.add(
        this.route.parent.params.subscribe(() => {
          if (this.targetNeighborhoodId) {
            this.loadMembers();
          }
        })
      );
    }
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
  }

  loadMembers(): void {
    const neighborhoodId = this.targetNeighborhoodId;
    if (!neighborhoodId) return;

    this.isLoading = true;
    const statusParam = this.statusFilter !== 'ALL' ? this.statusFilter : undefined;
    this.neighborhoodService.getMembers(neighborhoodId, statusParam).subscribe({
      next: (data) => {
        this.members = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast('Failed to load members: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  get filteredMembers(): Membership[] {
    return this.members.filter((m) => {
      if (this.statusFilter !== 'ALL' && m.status !== this.statusFilter) return false;
      if (this.searchQuery.trim()) {
        const q = this.searchQuery.toLowerCase().trim();
        const matchName = (m.fullName || '').toLowerCase().includes(q);
        const matchEmail = (m.email || '').toLowerCase().includes(q);
        const matchUnit = (m.unitNumber || '').toLowerCase().includes(q);
        return matchName || matchEmail || matchUnit;
      }
      return true;
    });
  }

  openInviteModal(): void {
    this.inviteForm.reset({
      email: '',
      fullName: '',
      unitNumber: '',
      phoneNumber: '',
      roles: 'RESIDENT'
    });
    this.showInviteModal = true;
    this.showSuccessModal = false;
    this.generatedInviteUrl = '';
  }

  closeInviteModal(): void {
    this.showInviteModal = false;
  }

  onInviteSubmit(): void {
    if (this.inviteForm.invalid || !this.targetNeighborhoodId) {
      this.inviteForm.markAllAsTouched();
      return;
    }

    const formVal = this.inviteForm.value;
    const payload = {
      email: formVal.email,
      fullName: formVal.fullName,
      unitNumber: formVal.unitNumber,
      phoneNumber: formVal.phoneNumber,
      roles: [formVal.roles]
    };

    this.neighborhoodService.inviteMember(this.targetNeighborhoodId, payload).subscribe({
      next: (created) => {
        const origin = window.location.origin;
        this.generatedInviteUrl = `${origin}/register?token=${created.invitationToken}`;
        this.showInviteModal = false;
        this.showSuccessModal = true;
        this.loadMembers();
      },
      error: (err) => {
        this.showToast('Failed to send invitation: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  copyLink(url: string): void {
    navigator.clipboard.writeText(url).then(() => {
      this.showToast('Invitation link copied to clipboard!', 'success');
    });
  }

  copyExistingInvite(member: Membership): void {
    if (!member.invitationToken) return;
    const origin = window.location.origin;
    const link = `${origin}/register?token=${member.invitationToken}`;
    this.copyLink(link);
  }

  deleteMember(member: Membership): void {
    if (!confirm(`Are you sure you want to remove member ${member.fullName || member.email} from the community?`)) {
      return;
    }

    if (!this.targetNeighborhoodId) return;

    this.neighborhoodService.deleteMember(this.targetNeighborhoodId, member.membershipId).subscribe({
      next: () => {
        this.showToast('Member removed successfully.', 'success');
        this.loadMembers();
      },
      error: (err) => {
        this.showToast('Failed to remove member: ' + (err.error?.message || 'Server error'), 'error');
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
}
