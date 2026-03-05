import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegistroPontoRequest, RegistroPontoResponse, SaldoDTO, DiaJornadaDTO, HistoricoDiario, PageResult } from '../interfaces/ponto-dto';
import { environment } from '../../environments/environment'; 

@Injectable({
  providedIn: 'root'
})
export class PontoService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/api/pontos`;

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth-token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  registrar(): Observable<RegistroPontoResponse> {
    return this.http.post<RegistroPontoResponse>(this.API_URL, {}, {
      headers: this.getHeaders()
    });
  }

  getSaldo(inicio: string, fim: string): Observable<SaldoDTO> {
    return this.http.get<SaldoDTO>(`${this.API_URL}/saldo?inicio=${inicio}&fim=${fim}`, {
      headers: this.getHeaders()
    });
  }

  getGraficoSemana(): Observable<DiaJornadaDTO[]> {
    return this.http.get<DiaJornadaDTO[]>(`${this.API_URL}/grafico`, {
      headers: this.getHeaders()
    });
  }

  buscarHistorico(dataInicio: string, dataFim: string, page: number, size: number): Observable<PageResult<HistoricoDiario>> {
    const params = new HttpParams()
      .set('dataInicio', dataInicio)
      .set('dataFim', dataFim)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResult<HistoricoDiario>>(`${this.API_URL}/historico`, {
      headers: this.getHeaders(),
      params: params
    });
  }
}