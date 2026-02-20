package com.dunk.espelhoponto.service;

import com.dunk.espelhoponto.dto.RegistroPontoResponseDTO;
import com.dunk.espelhoponto.dto.SaldoHorasDTO;
import com.dunk.espelhoponto.entity.Ponto;
import com.dunk.espelhoponto.entity.Usuario;
import com.dunk.espelhoponto.enums.RegraUsuario;
import com.dunk.espelhoponto.enums.TipoRegistro;
import com.dunk.espelhoponto.exception.RegraNegocioException;
import com.dunk.espelhoponto.repository.PontoRepository;
import com.dunk.espelhoponto.strategy.CalculoAdicionalNoturno;
import com.dunk.espelhoponto.strategy.CalculoFimDeSemana;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PontoServiceTest {

    @Mock
    private PontoRepository repository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private PontoService service;
    private Usuario usuarioPadrao;

    @BeforeEach
    void setup() {
        CalculoAdicionalNoturno estrategiaNoturna = new CalculoAdicionalNoturno();
        CalculoFimDeSemana estrategiaFds = new CalculoFimDeSemana();

        service = new PontoService(repository, estrategiaNoturna, estrategiaFds);

        usuarioPadrao = new Usuario(UUID.randomUUID(), "teste@dunk.com", "123", "Teste", RegraUsuario.USER);
    }


    @Test
    @DisplayName("Deve gerar aviso de infração de intervalo intrajornada (< 1h)")
    void deveGerarAvisoDeAlmocoCurto() {
        LocalDate data = LocalDate.of(2026, 1, 20);

        List<Ponto> pontos = Arrays.asList(
                criarPonto(data, 8, 0, TipoRegistro.ENTRADA),
                criarPonto(data, 12, 0, TipoRegistro.SAIDA),
                criarPonto(data, 12, 30, TipoRegistro.ENTRADA),
                criarPonto(data, 17, 30, TipoRegistro.SAIDA)
        );

        when(repository.findByUsuarioAndDataHoraBetweenOrderByDataHoraAsc(
                eq(usuarioPadrao), any(), any())).thenReturn(pontos);

        SaldoHorasDTO resultado = service.calcularBancoHoras(usuarioPadrao, data, data);

        assertFalse(resultado.avisos().isEmpty(), "Deveria ter avisos");
        assertTrue(resultado.avisos().get(0).contains("menor que 1h"));
        assertEquals(540L, resultado.minutosTrabalhados());
    }

    @Test
    @DisplayName("Deve registrar ENTRADA automaticamente se for a 1ª batida do dia (Contagem 0)")
    void deveRegistrarEntradaAutomaticamente() {
        try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(usuarioPadrao);

            when(repository.findTopByUsuarioOrderByDataHoraDesc(usuarioPadrao))
                    .thenReturn(Optional.empty());

            when(repository.countByUsuarioAndDataHoraBetween(eq(usuarioPadrao), any(), any()))
                    .thenReturn(0L);

            RegistroPontoResponseDTO response = service.registrar();

            assertEquals(TipoRegistro.ENTRADA, response.tipo());
            verify(repository, times(1)).save(any(Ponto.class));
        }
    }

    @Test
    @DisplayName("Deve registrar SAÍDA automaticamente se já existe 1 batida hoje (Contagem 1)")
    void deveRegistrarSaidaAutomaticamente() {
        try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(usuarioPadrao);

            Ponto pontoAnterior = criarPonto(LocalDate.now(), LocalTime.now().minusHours(1).getHour(), 0, TipoRegistro.ENTRADA);

            when(repository.findTopByUsuarioOrderByDataHoraDesc(usuarioPadrao))
                    .thenReturn(Optional.of(pontoAnterior));

            when(repository.countByUsuarioAndDataHoraBetween(eq(usuarioPadrao), any(), any()))
                    .thenReturn(1L);

            RegistroPontoResponseDTO response = service.registrar();

            assertEquals(TipoRegistro.SAIDA, response.tipo());
            verify(repository, times(1)).save(any(Ponto.class));
        }
    }

    @Test
    @DisplayName("Deve bloquear registro com menos de 5 minutos de diferença")
    void deveBloquearRegistroRapido() {
        Ponto pontoRecente = Ponto.builder()
                .usuario(usuarioPadrao)
                .dataHora(LocalDateTime.now())
                .tipo(TipoRegistro.ENTRADA)
                .build();

        try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(usuarioPadrao);

            when(repository.findTopByUsuarioOrderByDataHoraDesc(usuarioPadrao))
                    .thenReturn(Optional.of(pontoRecente));

            assertThrows(RegraNegocioException.class, () -> service.registrar());
        }
    }

    @Test
    @DisplayName("Deve bloquear o registro se o usuário já tiver 6 batidas no dia")
    void deveBloquearSetimaBatida() {
        try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(usuarioPadrao);

            when(repository.findTopByUsuarioOrderByDataHoraDesc(usuarioPadrao))
                    .thenReturn(Optional.empty());

            when(repository.countByUsuarioAndDataHoraBetween(eq(usuarioPadrao), any(), any()))
                    .thenReturn(6L);

            RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
                service.registrar();
            });

            assertEquals("Limite diário atingido! Você já realizou os 6 registros permitidos hoje.", exception.getMessage());

            verify(repository, never()).save(any(Ponto.class));
        }
    }

    private Ponto criarPonto(LocalDate data, int hora, int minuto, TipoRegistro tipo) {
        return Ponto.builder()
                .usuario(usuarioPadrao)
                .dataHora(LocalDateTime.of(data, LocalTime.of(hora, minuto)))
                .tipo(tipo)
                .build();
    }
}