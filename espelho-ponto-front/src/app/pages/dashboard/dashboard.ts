import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';

import { PontoService } from '../../services/ponto';
import { AuthService } from '../../services/auth';
import { SaldoDTO, DiaJornadaDTO } from '../../interfaces/ponto-dto';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class DashboardComponent implements OnInit {
  private pontoService = inject(PontoService);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  graficoSemana: { dia: string, horas: number, percentual: number }[] = [];

  saldoDados: SaldoDTO | null = null;
  hoje = new Date();

  nomeUsuario: string = '';
  isAdmin = false;

  ngOnInit() {
    const nomeCompleto = this.authService.getUserName();
    this.nomeUsuario = nomeCompleto.split(' ')[0];

    this.carregarSaldo();
    this.carregarGrafico();
    
    this.isAdmin = this.authService.hasRole('ADMIN'); 
  }

  carregarSaldo() {
    const inicio = new Date(this.hoje.getFullYear(), this.hoje.getMonth(), 1).toISOString().split('T')[0];
    const fim = new Date(this.hoje.getFullYear(), this.hoje.getMonth() + 1, 0).toISOString().split('T')[0];

    this.pontoService.getSaldo(inicio, fim).subscribe({
      next: (dados) => this.saldoDados = dados,
      error: (err) => console.error('Erro ao carregar saldo', err)
    });
  }

  carregarGrafico() {
    this.pontoService.getGraficoSemana().subscribe({
      next: (dados: DiaJornadaDTO[]) => {
        this.graficoSemana = dados.map(item => {
          const calcPercentual = (item.horas / 8) * 100;
          return {
            dia: item.dia,
            horas: item.horas,
            percentual: Math.min(calcPercentual, 100)
          };
        });
      },
      error: (err) => console.error('Erro ao carregar gráfico', err)
    });
  }

  registrarPonto() {
    this.pontoService.registrar().subscribe({
      next: (res) => {
        const msg = res.aviso ? `${res.mensagem} ⚠️ ${res.aviso}` : res.mensagem;
        const cor = res.tipo === 'ENTRADA' ? 'success-snackbar' : 'warning-snackbar';

        this.snackBar.open(msg, 'OK', {
          duration: 5000,
          panelClass: [cor]
        });

        this.carregarSaldo();
        this.carregarGrafico();
      },
      error: (err: any) => {
        const msgErro = err.error?.mensagem || 'Erro ao registrar ponto';
        this.snackBar.open(msgErro, 'Fechar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  removerAviso(index: number) {
    if (this.saldoDados && this.saldoDados.avisos) {
      this.saldoDados.avisos.splice(index, 1);
    }
  }

  irParaCadastro() {
    this.router.navigate(['/admin/usuarios']); 
  }
}