import { Injectable, inject } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse } from '../interfaces/auth-dto';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private readonly API_URL = '/auth';

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem('auth-token', response.token);
        localStorage.setItem('user-name', response.nome);
      }),
    );
  }

  getUserName(): string {
    return localStorage.getItem('user-name') || 'Usuário';
  }

  logout(): void {
    localStorage.removeItem('auth-token');
    localStorage.removeItem('user-name');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('auth-token');
  }

  hasRole(roleEsperada: string): boolean {
    const token = localStorage.getItem('auth-token');
    if (!token) return false;

    try {
      const decoded: any = jwtDecode(token);
      const target = roleEsperada.toUpperCase();

      if (decoded.regra === target) return true;

      if (decoded.role === target) return true;

      if (decoded.authorities && Array.isArray(decoded.authorities)) {
        return decoded.authorities.includes(`ROLE_${target}`) || decoded.authorities.includes(target);
      }

      return false;
    } catch (e) {
      return false;
    }
  }
}

