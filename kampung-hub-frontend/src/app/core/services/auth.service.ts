import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, throwError } from 'rxjs';
import { AuthResponse, LoginRequest, SwitchContextRequest } from '../models/auth.model';
import { MembershipContext } from '../models/membership.model';

// FE-Req-10 & 15: Service handling user session authentication and multi-neighborhood context switching
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/v1/auth';
  private readonly TOKEN_KEY = 'kampung_token';
  private readonly SESSION_KEY = 'kampung_session';

  private sessionSubject = new BehaviorSubject<AuthResponse | null>(this.getStoredSession());
  public session$ = this.sessionSubject.asObservable();

  constructor(private http: HttpClient) {}

  // FE-Req-10: Login HTTP POST returning AuthResponse Observable
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => this.setSession(response)),
      catchError((error) => throwError(() => error))
    );
  }

  // FE-Req-15: Switch active neighborhood context and update stored JWT token
  switchContext(membershipId: string): Observable<AuthResponse> {
    const current = this.sessionSubject.value;
    if (!current) {
      return throwError(() => new Error('No authenticated user session found.'));
    }

    const payload: SwitchContextRequest = {
      userId: current.userId,
      membershipId: membershipId
    };

    return this.http.post<AuthResponse>(`${this.apiUrl}/context`, payload).pipe(
      tap((response) => {
        // Retain the list of all available memberships while updating token and active membership
        const updatedResponse: AuthResponse = {
          ...response,
          memberships: current.memberships
        };
        this.setSession(updatedResponse);
      }),
      catchError((error) => throwError(() => error))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.SESSION_KEY);
    this.sessionSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUser(): AuthResponse | null {
    return this.sessionSubject.value;
  }

  getActiveMembership(): MembershipContext | null {
    return this.sessionSubject.value?.activeMembership || null;
  }

  getActiveMembershipId(): string {
    return this.sessionSubject.value?.activeMembership?.membershipId || '';
  }

  getActiveNeighborhoodId(): string {
    return this.sessionSubject.value?.activeMembership?.neighborhoodId || '';
  }

  getAvailableMemberships(): MembershipContext[] {
    return this.sessionSubject.value?.memberships || [];
  }

  hasRole(role: string): boolean {
    const roles = this.sessionSubject.value?.activeMembership?.roles || [];
    return roles.includes(role.toUpperCase());
  }

  private setSession(authResult: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, authResult.token);
    localStorage.setItem(this.SESSION_KEY, JSON.stringify(authResult));
    this.sessionSubject.next(authResult);
  }

  private getStoredSession(): AuthResponse | null {
    try {
      const session = localStorage.getItem(this.SESSION_KEY);
      return session ? JSON.parse(session) : null;
    } catch {
      return null;
    }
  }
}
