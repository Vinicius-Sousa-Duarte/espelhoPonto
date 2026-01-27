package com.dunk.espelhoponto.strategy;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class CalculoAdicionalNoturno implements CalculoHoraStrategy {

    private static final LocalTime INICIO_NOITE = LocalTime.of(22, 0);
    private static final LocalTime FIM_NOITE = LocalTime.of(5, 0);
    private static final double FATOR_NOTURNO = 1.2;

    @Override
    public Long calcular(LocalDateTime inicio, LocalDateTime fim) {
        double minutosTotais = 0.0;
        LocalDateTime cursor = inicio;

        while (cursor.isBefore(fim)) {
            LocalTime horaAtual = cursor.toLocalTime();
            boolean ehNoturno = horaAtual.compareTo(INICIO_NOITE) >= 0 || horaAtual.compareTo(FIM_NOITE) < 0;

            if (ehNoturno) {
                minutosTotais += 1.2;
            } else {
                minutosTotais += 1.0;
            }
            cursor = cursor.plusMinutes(1);
        }
        return (long) minutosTotais;
    }
}
