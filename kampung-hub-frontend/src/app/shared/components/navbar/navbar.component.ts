import { Component, OnInit, OnDestroy, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { AuthResponse } from '../../../core/models/auth.model';
import { MembershipContext } from '../../../core/models/membership.model';

// FE-Req-15: Persistent Navbar with active neighborhood switcher, role badges and profile management
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  session: AuthResponse | null = null;
  activeMembership: MembershipContext | null = null;
  availableMemberships: MembershipContext[] = [];

  isNeighborhoodDropdownOpen = false;
  isUserDropdownOpen = false;
  isMobileMenuOpen = false;
  isSwitchingContext = false;
  switchMessage = '';

  private sub = new Subscription();

  constructor(
    public authService: AuthService,
    private router: Router,
    private elementRef: ElementRef
  ) {}

  ngOnInit(): void {
    this.sub.add(
      this.authService.session$.subscribe((session) => {
        this.session = session;
        this.activeMembership = session?.activeMembership || null;
        this.availableMemberships = session?.memberships || [];
      })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  // FE-Req-15: Switch active neighborhood context dynamically
  switchNeighborhood(membership: MembershipContext): void {
    if (this.activeMembership?.membershipId === membership.membershipId) {
      this.isNeighborhoodDropdownOpen = false;
      return;
    }

    this.isSwitchingContext = true;
    this.switchMessage = `Switching context to ${membership.neighborhoodName}...`;
    this.isNeighborhoodDropdownOpen = false;

    this.authService.switchContext(membership.membershipId).subscribe({
      next: () => {
        setTimeout(() => {
          this.isSwitchingContext = false;
          this.switchMessage = '';
          // Reload current route or navigate to dashboard to refresh view data
          this.router.navigate(['/dashboard']);
        }, 500);
      },
      error: (err) => {
        this.isSwitchingContext = false;
        this.switchMessage = '';
        console.error('Failed to switch context', err);
        alert('Failed to switch neighborhood context: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }

  toggleNeighborhoodDropdown(event: Event): void {
    event.stopPropagation();
    this.isNeighborhoodDropdownOpen = !this.isNeighborhoodDropdownOpen;
    this.isUserDropdownOpen = false;
  }

  toggleUserDropdown(event: Event): void {
    event.stopPropagation();
    this.isUserDropdownOpen = !this.isUserDropdownOpen;
    this.isNeighborhoodDropdownOpen = false;
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  logout(): void {
    this.isUserDropdownOpen = false;
    this.isNeighborhoodDropdownOpen = false;
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isNeighborhoodDropdownOpen = false;
      this.isUserDropdownOpen = false;
    }
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
