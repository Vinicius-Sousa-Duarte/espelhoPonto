package com.dunk.espelhoponto.dto;

import java.util.List;

public record SaldoHorasDTO(
        String nomeFuncionario,
        String saldoTotal,
        long minutosTrabalhados,
        long minutosEsperados,
        List<String> avisos
) {}
