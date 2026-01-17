package com.dunk.espelhoponto.controller;

import com.dunk.espelhoponto.dto.NovoRegistroDTO;
import com.dunk.espelhoponto.dto.SaldoHorasDTO;
import com.dunk.espelhoponto.enums.TipoRegistro;
import com.dunk.espelhoponto.service.PontoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PontoControllerTest {

    @Mock
    private PontoService pontoService;

    private PontoController pontoController;

    @BeforeEach
    void setUp() {
        pontoController = new PontoController(pontoService, pontoService);
    }

    @Test
    @DisplayName("Deve retornar 201 Created ao bater ponto")
    void deveBaterPontoComSucesso() {
        NovoRegistroDTO dto = new NovoRegistroDTO("Vinicius", TipoRegistro.ENTRADA);

        ResponseEntity<Void> response = pontoController.baterPonto(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(pontoService, times(1)).registrar(dto);
    }

    @Test
    @DisplayName("Deve retornar saldo de horas corretamente")
    void deveConsultarSaldoComSucesso() {
        String nome = "Vinicius";
        LocalDate inicio = LocalDate.now().minusDays(5);
        LocalDate fim = LocalDate.now();

        SaldoHorasDTO saldoMock = new SaldoHorasDTO(nome, "+00:00", 480L, 480L);

        when(pontoService.calcularBancoHoras(nome, inicio, fim)).thenReturn(saldoMock);

        ResponseEntity<SaldoHorasDTO> response = pontoController.consultarSaldo(nome, inicio, fim);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saldoMock, response.getBody());
        verify(pontoService, times(1)).calcularBancoHoras(nome, inicio, fim);
    }
}