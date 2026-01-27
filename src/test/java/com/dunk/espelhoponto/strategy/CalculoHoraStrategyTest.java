package com.dunk.espelhoponto.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculoHoraStrategyTest {

    private final CalculoAdicionalNoturno noturnoStrategy = new CalculoAdicionalNoturno();
    private final CalculoFimDeSemana fdsStrategy = new CalculoFimDeSemana();

    @Test
    @DisplayName("Deve calcular horas normais sem adicional (13h às 14h)")
    void deveCalcularHoraNormal() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 13, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 14, 0);

        long resultado = noturnoStrategy.calcular(inicio, fim);

        assertEquals(60, resultado);
    }

    @Test
    @DisplayName("Deve aplicar adicional noturno de 20% (22h às 23h)")
    void deveCalcularHoraNoturna() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 22, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 23, 0);

        long resultado = noturnoStrategy.calcular(inicio, fim);

        // 60 minutos * 1.2 = 72 minutos
        assertEquals(72, resultado);
    }

    @Test
    @DisplayName("Deve calcular horário misto (04h às 06h)")
    void deveCalcularMisto() {
        // 04:00 às 05:00 (Noturno) + 05:00 às 06:00 (Normal)
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 4, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 6, 0);

        long resultado = noturnoStrategy.calcular(inicio, fim);

        // (60 * 1.2) + 60 = 72 + 60 = 132
        assertEquals(132, resultado);
    }

    @Test
    @DisplayName("Deve dobrar as horas no Sábado")
    void deveCalcularFimDeSemana() {
        // Sábado dia 24/01/2026
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 24, 8, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 24, 10, 0);
        long resultado = fdsStrategy.calcular(inicio, fim);

        // 120 minutos * 2.0 = 240
        assertEquals(240, resultado);
    }

    @Test
    @DisplayName("Não deve aplicar regra de FDS na Segunda-feira")
    void naoDeveCalcularFimDeSemanaNaSegunda() {
        // Segunda dia 26/01/2026
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 26, 8, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 26, 10, 0);

        long resultado = fdsStrategy.calcular(inicio, fim);

        assertEquals(0, resultado);
    }
}