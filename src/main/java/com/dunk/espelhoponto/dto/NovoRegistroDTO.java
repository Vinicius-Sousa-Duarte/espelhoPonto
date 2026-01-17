package com.dunk.espelhoponto.dto;

import com.dunk.espelhoponto.enums.TipoRegistro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NovoRegistroDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nomeFuncionario,

        @NotNull(message = "Tipo (ENTRADA/SAIDA) é obrigatório")
        TipoRegistro tipo
) {}


