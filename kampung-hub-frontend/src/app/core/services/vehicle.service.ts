import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateVehicleDto, ResidentVehicle, UpdateVehicleDto } from '../models/vehicle.model';

// FE-Req-10: Service for resident vehicle whitelist management
@Injectable({
  providedIn: 'root'
})
export class VehicleService {
  private readonly apiUrl = 'http://localhost:8080/api/v1/vehicles';

  constructor(private http: HttpClient) {}

  // FE-Req-10: GET HTTP request returning an Observable array of vehicles
  getVehicles(
    neighborhoodId?: string,
    ownerMembershipId?: string,
    status?: string,
    sortBy?: string
  ): Observable<ResidentVehicle[]> {
    let params = new HttpParams();
    if (neighborhoodId) params = params.set('neighborhoodId', neighborhoodId);
    if (ownerMembershipId) params = params.set('ownerMembershipId', ownerMembershipId);
    if (status) params = params.set('status', status);
    if (sortBy) params = params.set('sortBy', sortBy);

    return this.http.get<ResidentVehicle[]>(this.apiUrl, { params });
  }

  // Get specific vehicle details
  getVehicleById(id: string): Observable<ResidentVehicle> {
    return this.http.get<ResidentVehicle>(`${this.apiUrl}/${id}`);
  }

  // FE-Req-10: POST HTTP request to register a new vehicle
  createVehicle(data: CreateVehicleDto): Observable<ResidentVehicle> {
    return this.http.post<ResidentVehicle>(this.apiUrl, data);
  }

  // FE-Req-10: PUT HTTP request to update vehicle details
  updateVehicle(id: string, data: UpdateVehicleDto): Observable<ResidentVehicle> {
    return this.http.put<ResidentVehicle>(`${this.apiUrl}/${id}`, data);
  }

  // FE-Req-10: DELETE HTTP request to remove a vehicle
  deleteVehicle(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
