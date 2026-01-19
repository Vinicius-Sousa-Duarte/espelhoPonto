package com.dunk.espelhoponto.dto;

import com.dunk.espelhoponto.enums.TipoRegistro;
import jakarta.validation.constraints.NotNull;

public record NovoRegistroDTO(

        @NotNull(message = "Tipo (ENTRADA/SAIDA) é obrigatório")
        TipoRegistro tipo
) {}