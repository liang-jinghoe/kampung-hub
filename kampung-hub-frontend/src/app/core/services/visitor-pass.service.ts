import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateVisitorPassDto, VisitorPass } from '../models/visitor-pass.model';

// FE-Req-10: Service for pre-registering visitor passes and gatehouse verification
@Injectable({
  providedIn: 'root'
})
export class VisitorPassService {
  private readonly apiUrl = 'http://localhost:8080/api/v1/visitor-passes';

  constructor(private http: HttpClient) {}

  searchVisitorPasses(
    neighborhoodId?: string,
    unitNumber?: string,
    passToken?: string,
    status?: string
  ): Observable<VisitorPass[]> {
    let params = new HttpParams();
    if (neighborhoodId) params = params.set('neighborhoodId', neighborhoodId);
    if (unitNumber) params = params.set('unitNumber', unitNumber);
    if (passToken) params = params.set('passToken', passToken);
    if (status) params = params.set('status', status);

    return this.http.get<VisitorPass[]>(this.apiUrl, { params });
  }

  createVisitorPass(data: CreateVisitorPassDto): Observable<VisitorPass> {
    return this.http.post<VisitorPass>(this.apiUrl, data);
  }

  checkInVisitorPass(id: string): Observable<VisitorPass> {
    return this.http.put<VisitorPass>(`${this.apiUrl}/${id}/check-in`, {});
  }
}
