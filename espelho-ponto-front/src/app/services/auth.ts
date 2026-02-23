import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
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
    const token = localStorage.getItem('auth-token');
    return !!token;
  }

  hasRole(roleEsperada: string): boolean {
    const token = localStorage.getItem('auth-token');
    if (!token) return false;

    try {
      const decoded: any = jwtDecode(token);
      const target = roleEsperada.toUpperCase();

      console.log('🔍 MEU TOKEN DECODIFICADO:', decoded);

      if (decoded.role) {
        const role = decoded.role.toString().toUpperCase();
        if (role === target || role === `ROLE_${target}`) return true;
      }

      if (decoded.regra) {
        const regra = decoded.regra.toString().toUpperCase();
        if (regra === target || regra === `ROLE_${target}`) return true;
      }

      if (decoded.authorities && Array.isArray(decoded.authorities)) {
        return decoded.authorities.some((auth: string) => {
          const authUpper = auth.toUpperCase();
          return authUpper === target || authUpper === `ROLE_${target}`;
        });
      }

      return false;
    } catch (error) {
      console.error('Erro ao decodificar token', error);
      return false;
    }
  }
}