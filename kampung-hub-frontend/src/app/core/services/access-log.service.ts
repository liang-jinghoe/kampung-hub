import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccessLog, CreateAccessLogDto } from '../models/access-log.model';

// FE-Req-10: Service for gate checkpoint logs
@Injectable({
  providedIn: 'root'
})
export class AccessLogService {
  private readonly apiUrl = 'http://localhost:8080/api/v1/access-logs';

  constructor(private http: HttpClient) {}

  getAccessLogs(
    neighborhoodId?: string,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'timestamp'
  ): Observable<AccessLog[]> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy);

    if (neighborhoodId) {
      params = params.set('neighborhoodId', neighborhoodId);
    }

    return this.http.get<AccessLog[]>(this.apiUrl, { params });
  }

  createAccessLog(data: CreateAccessLogDto): Observable<AccessLog> {
    return this.http.post<AccessLog>(this.apiUrl, data);
  }
}
