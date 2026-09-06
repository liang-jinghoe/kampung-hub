import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { NeighborhoodService } from '../../../core/services/neighborhood.service';
import { AuthService } from '../../../core/services/auth.service';
import { Neighborhood, CreateNeighborhoodDto } from '../../../core/models/neighborhood.model';

// FE-Req-12, 14: Neighborhoods Directory with Query Param Tier Filtering and Programmatic Navigation
@Component({
  selector: 'app-neighborhood-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './neighborhood-list.component.html',
  styleUrls: ['./neighborhood-list.component.css']
})
export class NeighborhoodListComponent implements OnInit, OnDestroy {
  neighborhoods: Neighborhood[] = [];
  isLoading = true;
  searchQuery = '';
  activeTierFilter = '';

  showCreateModal = false;
  createForm: FormGroup;
  toast: { message: string; type: 'success' | 'error' } | null = null;
  private toastTimeout: any;

  private sub = new Subscription();

  constructor(
    private neighborhoodService: NeighborhoodService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private fb: FormBuilder
  ) {
    this.createForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      propertyType: ['LANDED', [Validators.required]],
      subscriptionTier: ['PREMIUM_LANDED', [Validators.required]],
      maxAllowedUnits: [100, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.sub.add(
      this.route.queryParams.subscribe((params) => {
        this.activeTierFilter = params['tier'] || '';
        this.loadNeighborhoods();
      })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
  }

  loadNeighborhoods(): void {
    this.isLoading = true;
    this.neighborhoodService.getNeighborhoods(this.activeTierFilter || undefined).subscribe({
      next: (data) => {
        this.neighborhoods = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast('Failed to load neighborhoods: ' + (err.error?.message || 'Server error'), 'error');
      }
    });
  }

  // FE-Req-14: Programmatic filter update via query params
  setTierFilter(tier: string): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: tier ? { tier } : {},
      queryParamsHandling: ''
    });
  }

  // FE-Req-12: Programmatic navigation to neighborhood detail view
  viewNeighborhood(id: string): void {
    this.router.navigate(['/neighborhoods', id]);
  }

  get adminNeighborhoodIds(): string[] {
    const memberships = this.authService.getAvailableMemberships();
    return memberships
      .filter((m) => m.roles.some((r) => r === 'ADMIN' || r === 'COMMITTEE'))
      .map((m) => m.neighborhoodId);
  }

  get filteredNeighborhoods(): Neighborhood[] {
    // Restrict neighborhoods directory to communities where current user has Admin/Committee responsibilities
    const adminIds = this.adminNeighborhoodIds;
    let list = this.neighborhoods;
    if (adminIds.length > 0) {
      list = list.filter((n) => adminIds.includes(n.id));
    }

    if (!this.searchQuery.trim()) return list;
    const q = this.searchQuery.toLowerCase().trim();
    return list.filter((n) => 
      n.name.toLowerCase().includes(q) || n.id.toLowerCase().includes(q)
    );
  }

  openCreateModal(): void {
    this.createForm.reset({
      name: '',
      propertyType: 'LANDED',
      subscriptionTier: 'PREMIUM_LANDED',
      maxAllowedUnits: 100
    });
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  onCreateSubmit(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const payload: CreateNeighborhoodDto = this.createForm.value;
    this.neighborhoodService.createNeighborhood(payload).subscribe({
      next: (created) => {
        this.showToast(`Neighborhood ${created.name} registered successfully!`, 'success');
        this.closeCreateModal();
        this.loadNeighborhoods();
      },
      error: (err) => {
        this.showToast('Failed to create neighborhood: ' + (err.error?.message || 'Server error'), 'error');
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
    return this.authService.hasRole('ADMIN');
  }
}
