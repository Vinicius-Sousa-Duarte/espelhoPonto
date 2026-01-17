package com.dunk.espelhoponto.dto;

public record SaldoHorasDTO(
        String nomeFuncionario,
        String saldoTotal,
        long minutosTrabalhados,
        long minutosEsperados
) {}
