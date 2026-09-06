import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateNeighborhoodDto, Neighborhood } from '../models/neighborhood.model';
import { Membership } from '../models/membership.model';

// FE-Req-10: Service for managing Neighborhood resources and member directories
@Injectable({
  providedIn: 'root'
})
export class NeighborhoodService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/neighborhoods';
  private readonly membersUrl = 'http://localhost:8080/api/v1/members';

  constructor(private http: HttpClient) {}

  // List all neighborhoods with optional tier or status filters
  getNeighborhoods(tier?: string, status?: string): Observable<Neighborhood[]> {
    let params = new HttpParams();
    if (tier) params = params.set('tier', tier);
    if (status) params = params.set('status', status);
    return this.http.get<Neighborhood[]>(this.baseUrl, { params });
  }

  // Get details for a specific neighborhood
  getNeighborhoodById(id: string): Observable<Neighborhood> {
    return this.http.get<Neighborhood>(`${this.baseUrl}/${id}`);
  }

  // Register a new neighborhood
  createNeighborhood(data: CreateNeighborhoodDto): Observable<Neighborhood> {
    return this.http.post<Neighborhood>(this.baseUrl, data);
  }

  // List all memberships for a neighborhood
  getMembers(neighborhoodId: string, status?: string, role?: string): Observable<Membership[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (role) params = params.set('role', role);
    return this.http.get<Membership[]>(`${this.baseUrl}/${neighborhoodId}/members`, { params });
  }

  // Direct register member
  directRegisterMember(neighborhoodId: string, data: any): Observable<Membership> {
    return this.http.post<Membership>(`${this.baseUrl}/${neighborhoodId}/members/direct`, data);
  }

  // Invite member
  inviteMember(neighborhoodId: string, data: any): Observable<Membership> {
    return this.http.post<Membership>(`${this.baseUrl}/${neighborhoodId}/members/invite`, data);
  }

  // Update member
  updateMember(neighborhoodId: string, membershipId: string, data: any): Observable<Membership> {
    return this.http.put<Membership>(`${this.baseUrl}/${neighborhoodId}/members/${membershipId}`, data);
  }

  // Delete member
  deleteMember(neighborhoodId: string, membershipId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${neighborhoodId}/members/${membershipId}`);
  }

  // Public onboarding registration with token
  onboardMember(token: string, data: any): Observable<Membership> {
    const params = new HttpParams().set('token', token);
    return this.http.post<Membership>(`${this.membersUrl}/register`, data, { params });
  }
}
