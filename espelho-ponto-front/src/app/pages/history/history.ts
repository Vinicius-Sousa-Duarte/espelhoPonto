import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { PontoService } from '../../services/ponto';
import { PontoRegistro } from '../../interfaces/ponto-dto';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatNativeDateModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './history.html',
  styleUrls: ['./history.scss'],
})
export class HistoryComponent implements OnInit {
  private pontoService = inject(PontoService);

  displayedColumns: string[] = ['data', 'hora', 'tipo', 'status'];
  dataSource: PontoRegistro[] = [];

  range = new FormGroup({
    start: new FormControl<Date | null>(
      new Date(new Date().getFullYear(), new Date().getMonth(), 1),
    ),
    end: new FormControl<Date | null>(new Date()),
  });

  ngOnInit() {
    this.buscarHistorico();
  }

  private formatarDataLocal(data: Date): string {
    const ano = data.getFullYear();
    const mes = (data.getMonth() + 1).toString().padStart(2, '0');
    const dia = data.getDate().toString().padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
  }

  buscarHistorico() {
    const start = this.range.value.start;
    const end = this.range.value.end;

    if (start && end) {
      const inicioStr = this.formatarDataLocal(start);
      const fimStr = this.formatarDataLocal(end);

      this.pontoService.getHistorico(inicioStr, fimStr).subscribe({
        next: (dados) => {
          this.dataSource = dados.sort(
            (a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime(),
          );
        },
        error: (err) => console.error('Erro ao buscar histórico', err),
      });
    }
  }
}
