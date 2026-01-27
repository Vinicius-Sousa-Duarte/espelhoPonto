package com.dunk.espelhoponto.dto;

import com.dunk.espelhoponto.enums.TipoRegistro;
import java.time.LocalDateTime;

public record RegistroPontoResponseDTO(
        String mensagem,
        String aviso,
        TipoRegistro tipo,
        LocalDateTime dataHora
) {}