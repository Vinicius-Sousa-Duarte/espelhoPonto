import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegistroPontoRequest, RegistroPontoResponse, SaldoDTO } from '../interfaces/ponto-dto';

@Injectable({
  providedIn: 'root'
})
export class PontoService {
  private http = inject(HttpClient);
  private readonly API_URL = '/api/pontos'; 

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth-token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  registrar(tipo: 'ENTRADA' | 'SAIDA'): Observable<RegistroPontoResponse> {
    const body: RegistroPontoRequest = { tipo };
    return this.http.post<RegistroPontoResponse>(this.API_URL, body, {
      headers: this.getHeaders()
    });
  }

  getSaldo(inicio: string, fim: string): Observable<SaldoDTO> {
    return this.http.get<SaldoDTO>(`${this.API_URL}/saldo?inicio=${inicio}&fim=${fim}`, {
      headers: this.getHeaders()
    });
  }
}