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

export interface PontoRegistro {
  id: string;
  dataHora: string;
  tipo: 'ENTRADA' | 'SAIDA';
  observacao?: string; 
}