package com.dunk.espelhoponto.dto;

import java.time.LocalDate;
import java.util.List;

public record HistoricoDiarioDTO(
        LocalDate data,
        List<String> marcacoes,
        long totalMinutosTrabalhados,
        String horasFormatadas,
        String status
) {}