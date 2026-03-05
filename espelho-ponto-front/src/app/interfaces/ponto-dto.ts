export interface RegistroPontoRequest {
  tipo: 'ENTRADA' | 'SAIDA';
}

export interface RegistroPontoResponse {
  mensagem: string;
  aviso?: string;
  tipo: string;
  dataHora: string;
}

export interface SaldoDTO {
  nomeFuncionario: string;
  saldoTotal: string;
  minutosTrabalhados: number;
  minutosEsperados: number;
  avisos: string[];
}

export interface DiaJornadaDTO {
  dia: string;
  horas: number;
}

export interface HistoricoDiario {
  data: string;
  marcacoes: string[];
  totalMinutosTrabalhados: number;
  horasFormatadas: string;
  status: string; 
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}