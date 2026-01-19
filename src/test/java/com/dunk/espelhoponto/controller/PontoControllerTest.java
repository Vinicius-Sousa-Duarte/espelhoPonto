package com.dunk.espelhoponto.controller;

import com.dunk.espelhoponto.entity.Usuario;
import com.dunk.espelhoponto.dto.NovoRegistroDTO;
import com.dunk.espelhoponto.dto.SaldoHorasDTO;
import com.dunk.espelhoponto.enums.RegraUsuario;
import com.dunk.espelhoponto.enums.TipoRegistro;
import com.dunk.espelhoponto.service.PontoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PontoControllerTest {

    @Mock
    private PontoService pontoService;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private PontoController pontoController;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario(UUID.randomUUID(), "vinicius@email.com", "senha", RegraUsuario.USER);
    }

    @Test
    @DisplayName("Deve retornar 201 Created ao bater ponto")
    void deveBaterPontoComSucesso() {
        NovoRegistroDTO dto = new NovoRegistroDTO(TipoRegistro.ENTRADA);

        ResponseEntity<Void> response = pontoController.baterPonto(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(pontoService, times(1)).registrar(dto);
    }

    @Test
    @DisplayName("Deve retornar saldo de horas pegando usuário do token")
    void deveConsultarSaldoComSucesso() {
        LocalDate inicio = LocalDate.now().minusDays(5);
        LocalDate fim = LocalDate.now();

        SaldoHorasDTO saldoMock = new SaldoHorasDTO("Vinicius", "+00:00", 480L, 480L);

        try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(usuarioMock);

            when(pontoService.calcularBancoHoras(eq(usuarioMock), eq(inicio), eq(fim)))
                    .thenReturn(saldoMock);

            ResponseEntity<SaldoHorasDTO> response = pontoController.consultarSaldo(inicio, fim);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(saldoMock, response.getBody());

            verify(pontoService, times(1)).calcularBancoHoras(usuarioMock, inicio, fim);
        }
    }
}