import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

import { AuthRequest, AuthResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'filesmove.auth.token';
  private readonly expiresAtKey = 'filesmove.auth.expiresAt';
  private readonly usernameKey = 'filesmove.auth.username';

  private readonly authState = new BehaviorSubject<boolean>(this.hasValidSession());
  readonly isAuthenticated$ = this.authState.asObservable();

  constructor(private readonly http: HttpClient) {}

  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', payload).pipe(
      tap((response) => this.persistSession(response))
    );
  }

  logout(): void {
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem(this.expiresAtKey);
    sessionStorage.removeItem(this.usernameKey);
    this.authState.next(false);
  }

  isAuthenticated(): boolean {
    const valid = this.hasValidSession();
    if (!valid) {
      this.logout();
    }
    return valid;
  }

  token(): string | null {
    return sessionStorage.getItem(this.tokenKey);
  }

  currentUsername(): string {
    return sessionStorage.getItem(this.usernameKey) ?? 'usuario';
  }

  private persistSession(response: AuthResponse): void {
    const expiresAt = Date.now() + response.expiresIn * 1000;
    const username = this.extractUsername(response.accessToken) ?? 'usuario';

    sessionStorage.setItem(this.tokenKey, response.accessToken);
    sessionStorage.setItem(this.expiresAtKey, String(expiresAt));
    sessionStorage.setItem(this.usernameKey, username);
    this.authState.next(true);
  }

  private hasValidSession(): boolean {
    const token = sessionStorage.getItem(this.tokenKey);
    const expiresAtRaw = sessionStorage.getItem(this.expiresAtKey);
    if (!token || !expiresAtRaw) {
      return false;
    }

    const expiresAt = Number(expiresAtRaw);
    if (Number.isNaN(expiresAt)) {
      return false;
    }

    return Date.now() < expiresAt;
  }

  private extractUsername(token: string): string | null {
    try {
      const parts = token.split('.');
      if (parts.length < 2) {
        return null;
      }
      const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
      return payload.sub ?? null;
    } catch {
      return null;
    }
  }
}
