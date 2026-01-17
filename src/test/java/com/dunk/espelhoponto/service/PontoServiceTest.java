package com.dunk.espelhoponto.service;

import com.dunk.espelhoponto.dto.NovoRegistroDTO;
import com.dunk.espelhoponto.dto.SaldoHorasDTO;
import com.dunk.espelhoponto.entity.Ponto;
import com.dunk.espelhoponto.enums.TipoRegistro;
import com.dunk.espelhoponto.repository.PontoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PontoServiceTest {

    @Mock
    private PontoRepository repository;

    @InjectMocks
    private PontoService service;

    @Test
    @DisplayName("Deve salvar um novo registro de ponto")
    void deveRegistrarPonto() {

        NovoRegistroDTO dto = new NovoRegistroDTO("Vinicius", TipoRegistro.ENTRADA);

        service.registrar(dto);

        ArgumentCaptor<Ponto> captor = ArgumentCaptor.forClass(Ponto.class);
        verify(repository).save(captor.capture());

        Ponto pontoSalvo = captor.getValue();
        assertEquals("Vinicius", pontoSalvo.getNomeFuncionario());
        assertEquals(TipoRegistro.ENTRADA, pontoSalvo.getTipo());
        assertNotNull(pontoSalvo.getDataHora());
    }

    @Test
    @DisplayName("Deve calcular saldo zerado (trabalhou exatamente 8 horas)")
    void deveCalcularSaldoZerado() {

        LocalDate data = LocalDate.of(2026, 1, 15);
        String nome = "Vinicius";

        List<Ponto> pontos = Arrays.asList(
                criarPonto(data, 8, 0, TipoRegistro.ENTRADA),
                criarPonto(data, 12, 0, TipoRegistro.SAIDA),
                criarPonto(data, 13, 0, TipoRegistro.ENTRADA),
                criarPonto(data, 17, 0, TipoRegistro.SAIDA)
        );

        when(repository.findByNomeFuncionarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(nome), any(), any())).thenReturn(pontos);

        SaldoHorasDTO resultado = service.calcularBancoHoras(nome, data, data);

        // Esperado: 8h * 60 = 480 min. Trabalhado: 480 min. Saldo: 0
        assertEquals(480L, resultado.minutosTrabalhados());
        assertEquals(480L, resultado.minutosTrabalhados());
        assertEquals("+00:00", resultado.saldoTotal());
    }

    @Test
    @DisplayName("Deve calcular saldo positivo (Hora Extra)")
    void deveCalcularSaldoPositivo() {
        LocalDate data = LocalDate.of(2026, 1, 15);
        String nome = "Vinicius";

        List<Ponto> pontos = Arrays.asList(
                criarPonto(data, 8, 0, TipoRegistro.ENTRADA),
                criarPonto(data, 17, 0, TipoRegistro.SAIDA)
        );

        when(repository.findByNomeFuncionarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(nome), any(), any())).thenReturn(pontos);

        SaldoHorasDTO resultado = service.calcularBancoHoras(nome, data, data);

        // Trabalhado: 9h (540 min). Esperado: 8h (480 min). Saldo: +60 min (+01:00)
        assertEquals(540L, resultado.minutosTrabalhados());
        assertEquals("+01:00", resultado.saldoTotal());
    }

    @Test
    @DisplayName("Deve calcular saldo negativo (Atraso/Falta)")
    void deveCalcularSaldoNegativo() {

        LocalDate data = LocalDate.of(2026, 1, 15);
        String nome = "Vinicius";

        List<Ponto> pontos = Arrays.asList(
                criarPonto(data, 8, 0, TipoRegistro.ENTRADA),
                criarPonto(data, 12, 0, TipoRegistro.SAIDA)
        );

        when(repository.findByNomeFuncionarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(nome), any(), any())).thenReturn(pontos);

        SaldoHorasDTO resultado = service.calcularBancoHoras(nome, data, data);

        // Trabalhado: 240 min. Esperado: 480 min. Saldo: -240 min (-04:00)
        assertEquals(240L, resultado.minutosTrabalhados());
        assertEquals("-04:00", resultado.saldoTotal());
    }

    @Test
    @DisplayName("Deve ignorar registro sem par (Saída faltando)")
    void deveIgnorarRegistroSemPar() {

        LocalDate data = LocalDate.of(2026, 1, 15);
        String nome = "Vinicius";

        List<Ponto> pontos = Arrays.asList(
                criarPonto(data, 8, 0, TipoRegistro.ENTRADA),
                criarPonto(data, 12, 0, TipoRegistro.ENTRADA) // Quebra a sequência logica
        );

        when(repository.findByNomeFuncionarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(nome), any(), any())).thenReturn(pontos);

        SaldoHorasDTO resultado = service.calcularBancoHoras(nome, data, data);

        // Nenhuma par ENTRADA->SAIDA formado. 0 trabalhados.
        assertEquals(0L, resultado.minutosTrabalhados());
        assertEquals("-08:00", resultado.saldoTotal());
    }

    @Test
    @DisplayName("Deve retornar vazio se não houver registros")
    void deveRetornarVazioSemRegistros() {
        String nome = "Vinicius";
        when(repository.findByNomeFuncionarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(nome), any(), any())).thenReturn(Collections.emptyList());

        SaldoHorasDTO resultado = service.calcularBancoHoras(nome, LocalDate.now(), LocalDate.now());

        assertEquals(0L, resultado.minutosTrabalhados());
        assertEquals(0L, resultado.minutosEsperados());
        assertEquals("+00:00", resultado.saldoTotal());
    }

    // Método auxiliar para criar Ponto
    private Ponto criarPonto(LocalDate data, int hora, int minuto, TipoRegistro tipo) {
        return Ponto.builder()
                .nomeFuncionario("Vinicius")
                .dataHora(LocalDateTime.of(data, LocalTime.of(hora, minuto)))
                .tipo(tipo)
                .build();
    }
}