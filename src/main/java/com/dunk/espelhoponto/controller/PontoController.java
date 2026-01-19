package com.dunk.espelhoponto.controller;

import com.dunk.espelhoponto.dto.NovoRegistroDTO;
import com.dunk.espelhoponto.dto.SaldoHorasDTO;
import com.dunk.espelhoponto.entity.Usuario;
import com.dunk.espelhoponto.service.PontoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/pontos")
@RequiredArgsConstructor
public class PontoController {

    private final PontoService service;

    @PostMapping
    public ResponseEntity<Void> baterPonto(@RequestBody @Valid NovoRegistroDTO dto) {
        service.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/saldo")
    public ResponseEntity<SaldoHorasDTO> consultarSaldo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var saldo = service.calcularBancoHoras(usuarioLogado, inicio, fim);

        return ResponseEntity.ok(saldo);
    }
}