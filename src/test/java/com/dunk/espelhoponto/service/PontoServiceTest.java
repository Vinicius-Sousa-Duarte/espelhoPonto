package com.dunk.espelhoponto.service;

import com.dunk.espelhoponto.entity.Usuario;
import com.dunk.espelhoponto.dto.NovoRegistroDTO;
import com.dunk.espelhoponto.dto.SaldoHorasDTO;
import com.dunk.espelhoponto.entity.Ponto;
import com.dunk.espelhoponto.enums.TipoRegistro;
import com.dunk.espelhoponto.enums.RegraUsuario;
import com.dunk.espelhoponto.repository.PontoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PontoService service;

    private Usuario usuarioPadrao;

    @BeforeEach
    void setup() {
        usuarioPadrao = new Usuario(UUID.randomUUID(), "vinicius@dunk.com", "senha123", RegraUsuario.USER);
    }

    @Test
    @DisplayName("Deve salvar um novo registro pegando o usuário do Contexto de Segurança")
    void deveRegistrarPonto() {
        NovoRegistroDTO dto = new NovoRegistroDTO(TipoRegistro.ENTRADA);

        try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(usuarioPadrao);

            service.registrar(dto);

            ArgumentCaptor<Ponto> captor = ArgumentCaptor.forClass(Ponto.class);
            verify(repository).save(captor.capture());

            Ponto pontoSalvo = captor.getValue();

            assertEquals(usuarioPadrao, pontoSalvo.getUsuario());
            assertEquals(TipoRegistro.ENTRADA, pontoSalvo.getTipo());
            assertNotNull(pontoSalvo.getDataHora());
        }
    }

    @Test
    @DisplayName("Deve calcular saldo zerado (trabalhou exatamente 8 horas)")
    void deveCalcularSaldoZerado() {
        LocalDate data = LocalDate.of(2026, 1, 15);

        List<Ponto> pontos = Arrays.asList(
                criarPonto(usuarioPadrao, data, 8, 0, TipoRegistro.ENTRADA),
                criarPonto(usuarioPadrao, data, 12, 0, TipoRegistro.SAIDA),
                criarPonto(usuarioPadrao, data, 13, 0, TipoRegistro.ENTRADA),
                criarPonto(usuarioPadrao, data, 17, 0, TipoRegistro.SAIDA)
        );

        when(repository.findByUsuarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(usuarioPadrao), any(), any())).thenReturn(pontos);

        SaldoHorasDTO resultado = service.calcularBancoHoras(usuarioPadrao, data, data);

        assertEquals(480L, resultado.minutosTrabalhados());
        assertEquals("+00:00", resultado.saldoTotal());
    }

    @Test
    @DisplayName("Deve calcular saldo positivo (Hora Extra)")
    void deveCalcularSaldoPositivo() {
        LocalDate data = LocalDate.of(2026, 1, 15);

        List<Ponto> pontos = Arrays.asList(
                criarPonto(usuarioPadrao, data, 8, 0, TipoRegistro.ENTRADA),
                criarPonto(usuarioPadrao, data, 17, 0, TipoRegistro.SAIDA) // 9 horas direto
        );

        when(repository.findByUsuarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(usuarioPadrao), any(), any())).thenReturn(pontos);

        SaldoHorasDTO resultado = service.calcularBancoHoras(usuarioPadrao, data, data);

        // Trabalhado: 540 min (9h). Esperado: 480 min (8h). Saldo: +60
        assertEquals(540L, resultado.minutosTrabalhados());
        assertEquals("+01:00", resultado.saldoTotal());
    }

    @Test
    @DisplayName("Deve calcular saldo negativo (Atraso/Falta)")
    void deveCalcularSaldoNegativo() {
        LocalDate data = LocalDate.of(2026, 1, 15);

        List<Ponto> pontos = Arrays.asList(
                criarPonto(usuarioPadrao, data, 8, 0, TipoRegistro.ENTRADA),
                criarPonto(usuarioPadrao, data, 12, 0, TipoRegistro.SAIDA) // Só 4h
        );

        when(repository.findByUsuarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(usuarioPadrao), any(), any())).thenReturn(pontos);

        SaldoHorasDTO resultado = service.calcularBancoHoras(usuarioPadrao, data, data);

        // Trabalhado: 240. Esperado: 480. Saldo: -240
        assertEquals("-04:00", resultado.saldoTotal());
    }

    @Test
    @DisplayName("Deve ignorar registro sem par (Saída faltando)")
    void deveIgnorarRegistroSemPar() {
        LocalDate data = LocalDate.of(2026, 1, 15);

        List<Ponto> pontos = Arrays.asList(
                criarPonto(usuarioPadrao, data, 8, 0, TipoRegistro.ENTRADA),
                criarPonto(usuarioPadrao, data, 12, 0, TipoRegistro.ENTRADA) // Erro: Duas entradas
        );

        when(repository.findByUsuarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(usuarioPadrao), any(), any())).thenReturn(pontos);

        SaldoHorasDTO resultado = service.calcularBancoHoras(usuarioPadrao, data, data);

        assertEquals(0L, resultado.minutosTrabalhados());
        assertEquals("-08:00", resultado.saldoTotal());
    }

    @Test
    @DisplayName("Deve retornar vazio se não houver registros")
    void deveRetornarVazioSemRegistros() {
        when(repository.findByUsuarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(usuarioPadrao), any(), any())).thenReturn(Collections.emptyList());

        SaldoHorasDTO resultado = service.calcularBancoHoras(usuarioPadrao, LocalDate.now(), LocalDate.now());

        assertEquals(0L, resultado.minutosTrabalhados());
        assertEquals(0L, resultado.minutosEsperados());
        assertEquals("+00:00", resultado.saldoTotal());
    }

    private Ponto criarPonto(Usuario usuario, LocalDate data, int hora, int minuto, TipoRegistro tipo) {
        return Ponto.builder()
                .usuario(usuario)
                .dataHora(LocalDateTime.of(data, LocalTime.of(hora, minuto)))
                .tipo(tipo)
                .build();
    }
}