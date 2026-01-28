package com.dunk.espelhoponto.auth.dto;

import com.dunk.espelhoponto.enums.RegraUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
        @NotBlank(message = "O login é obrigatório")
        @Email(message = "O login deve ser um email válido")
        String login,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha,

        @NotNull(message = "A regra de acesso (role) é obrigatória")
        RegraUsuario regra
) {}