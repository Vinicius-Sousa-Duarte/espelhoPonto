package com.dunk.espelhoponto.dto;

import com.dunk.espelhoponto.enums.TipoRegistro;

public record NovoRegistroDTO(
        String nomeFuncionario,
        TipoRegistro tipo
) {}


