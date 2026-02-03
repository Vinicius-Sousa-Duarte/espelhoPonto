import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PontoService } from '../../services/ponto';
import { SaldoDTO } from '../../interfaces/ponto-dto';
import { AuthService } from '../../services/auth';

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
  private snackBar = inject(MatSnackBar);
  private authService = inject(AuthService);

  saldoDados: SaldoDTO | null = null;
  hoje = new Date();
  nomeUsuario: string = '';

  ngOnInit() {
    const nomeCompleto = this.authService.getUserName();
    this.nomeUsuario = nomeCompleto.split(' ')[0];

    this.carregarSaldo();
  }
  private formatarDataLocal(data: Date): string {
    const ano = data.getFullYear();
    const mes = (data.getMonth() + 1).toString().padStart(2, '0');
    const dia = data.getDate().toString().padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
  }

  carregarSaldo() {
    const dataInicio = new Date(this.hoje.getFullYear(), this.hoje.getMonth(), 1);
    const dataFim = new Date(this.hoje.getFullYear(), this.hoje.getMonth() + 1, 0);

    const inicio = this.formatarDataLocal(dataInicio);
    const fim = this.formatarDataLocal(dataFim);

    this.pontoService.getSaldo(inicio, fim).subscribe({
      next: (dados) => {
        this.saldoDados = dados;
      },
      error: (err) => {
        console.error('Erro ao carregar saldo', err);
        this.snackBar.open('Erro ao carregar dados. Tente recarregar a página.', 'Fechar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  registrarPonto(tipo: 'ENTRADA' | 'SAIDA') {
    this.pontoService.registrar(tipo).subscribe({
      next: (res) => {
        const msg = res.aviso ? `${res.mensagem} ⚠️ ${res.aviso}` : res.mensagem;
        this.snackBar.open(msg, 'OK', { duration: 5000 });
        this.carregarSaldo();
      },
      error: (err) => {
        const msgErro = err.error?.mensagem || 'Erro ao registrar ponto';
        this.snackBar.open(msgErro, 'Fechar', { duration: 5000, panelClass: ['error-snackbar'] });
      }
    });
  }
}
