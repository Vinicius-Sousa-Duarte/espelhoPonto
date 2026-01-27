package com.dunk.espelhoponto.strategy;

import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class CalculoFimDeSemana implements CalculoHoraStrategy {

    private static final double FATOR_FDS = 2.0;

    @Override
    public Long calcular(LocalDateTime inicio, LocalDateTime fim) {
        DayOfWeek dia = inicio.getDayOfWeek();
        if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY) {
            return 0L;
        }

        long minutosReais = ChronoUnit.MINUTES.between(inicio, fim);
        return (long) (minutosReais * FATOR_FDS);
    }
}