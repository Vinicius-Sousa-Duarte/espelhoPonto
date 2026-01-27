package com.dunk.espelhoponto.strategy;

import java.time.LocalDateTime;

public interface CalculoHoraStrategy {

    Long calcular(LocalDateTime inicio, LocalDateTime fim);
}
