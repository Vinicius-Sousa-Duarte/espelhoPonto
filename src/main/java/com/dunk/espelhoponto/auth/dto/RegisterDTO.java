package com.dunk.espelhoponto.auth.dto;

import com.dunk.espelhoponto.enums.RegraUsuario;

public record RegisterDTO(String login, String password, RegraUsuario role) {}
