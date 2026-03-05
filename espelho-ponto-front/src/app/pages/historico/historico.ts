import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';
import { PontoService } from '../../services/ponto';
import { HistoricoDiario } from '../../interfaces/ponto-dto';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { format } from 'date-fns';

@Component({
  selector: 'app-historico',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatNativeDateModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './historico.html',
  styleUrls: ['./historico.scss']
})
export class HistoricoComponent implements OnInit {
  colunas: string[] = ['data', 'marcacoes', 'horas', 'status'];
  dados: HistoricoDiario[] = [];
  
  totalElementos = 0;
  tamanhoPagina = 10;
  paginaAtual = 0;

  filtroForm: FormGroup;

  constructor(private fb: FormBuilder, private pontoService: PontoService) {
    const hoje = new Date();
    const primeiroDiaMes = new Date(hoje.getFullYear(), hoje.getMonth(), 1);

    this.filtroForm = this.fb.group({
      inicio: [primeiroDiaMes],
      fim: [hoje]
    });
  }

  ngOnInit(): void {
    this.carregarHistorico();
  }

  carregarHistorico(): void {
    const inicioDate = this.filtroForm.get('inicio')?.value;
    const fimDate = this.filtroForm.get('fim')?.value;

    if (!inicioDate || !fimDate) return;

    const inicioStr = format(inicioDate, 'yyyy-MM-dd');
    const fimStr = format(fimDate, 'yyyy-MM-dd');

    this.pontoService.buscarHistorico(inicioStr, fimStr, this.paginaAtual, this.tamanhoPagina)
      .subscribe({
        next: (response) => {
          this.dados = response.content;
          this.totalElementos = response.totalElements;
        },
        error: (err) => console.error('Erro ao buscar histórico', err)
      });
  }

  mudarPagina(event: PageEvent): void {
    this.paginaAtual = event.pageIndex;
    this.tamanhoPagina = event.pageSize;
    this.carregarHistorico();
  }

  aplicarFiltro(): void {
    this.paginaAtual = 0;
    this.carregarHistorico();
  }
}