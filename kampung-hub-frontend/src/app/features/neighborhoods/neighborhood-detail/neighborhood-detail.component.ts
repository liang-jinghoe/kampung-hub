import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { NeighborhoodService } from '../../../core/services/neighborhood.service';
import { Neighborhood } from '../../../core/models/neighborhood.model';
import { VehicleManagementComponent } from '../../vehicles/vehicle-management.component';

// FE-Req-13: Neighborhood Detail with Nested Sub-Views
@Component({
  selector: 'app-neighborhood-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, VehicleManagementComponent],
  templateUrl: './neighborhood-detail.component.html',
  styleUrls: ['./neighborhood-detail.component.css']
})
export class NeighborhoodDetailComponent implements OnInit, OnDestroy {
  neighborhoodId = '';
  neighborhood: Neighborhood | null = null;
  isLoading = true;
  activeTab: 'overview' | 'vehicles' | 'passes' | 'members' = 'overview';

  private sub = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private neighborhoodService: NeighborhoodService
  ) {}

  ngOnInit(): void {
    this.sub.add(
      this.route.params.subscribe((params) => {
        this.neighborhoodId = params['id'];
        if (this.neighborhoodId) {
          this.loadNeighborhood(this.neighborhoodId);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  loadNeighborhood(id: string): void {
    this.isLoading = true;
    this.neighborhoodService.getNeighborhoodById(id).subscribe({
      next: (data) => {
        this.neighborhood = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  setActiveTab(tab: 'overview' | 'vehicles' | 'passes' | 'members'): void {
    this.activeTab = tab;
  }
}
