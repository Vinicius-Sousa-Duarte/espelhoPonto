package com.dunk.espelhoponto.controller;

import com.dunk.espelhoponto.dto.NovoRegistroDTO;
import com.dunk.espelhoponto.dto.SaldoHorasDTO;
import com.dunk.espelhoponto.service.PontoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/pontos")
@RequiredArgsConstructor // Cria o construtor automaticamente para os campos 'final'
public class PontoController {

    private final PontoService service;

    @PostMapping
    public ResponseEntity<Void> baterPonto(@RequestBody @Valid NovoRegistroDTO dto) {
        service.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/saldo")
    public ResponseEntity<SaldoHorasDTO> consultarSaldo(
            @RequestParam String nome,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        var saldo = service.calcularBancoHoras(nome, inicio, fim);
        return ResponseEntity.ok(saldo);
    }
}