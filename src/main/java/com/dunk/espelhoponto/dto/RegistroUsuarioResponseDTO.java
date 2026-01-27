package com.dunk.espelhoponto.dto;

import com.dunk.espelhoponto.enums.RegraUsuario;
import java.time.LocalDateTime;

public record RegistroUsuarioResponseDTO(
        String login,
        RegraUsuario role,
        String mensagem,
        LocalDateTime dataCriacao
) {}