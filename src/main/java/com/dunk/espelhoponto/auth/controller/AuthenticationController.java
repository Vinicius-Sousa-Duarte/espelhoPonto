package com.dunk.espelhoponto.auth.controller;

import com.dunk.espelhoponto.auth.dto.AuthenticationDTO;
import com.dunk.espelhoponto.auth.dto.LoginResponseDTO;
import com.dunk.espelhoponto.auth.dto.RegisterDTO;
import com.dunk.espelhoponto.entity.Usuario;
import com.dunk.espelhoponto.dto.RegistroUsuarioResponseDTO;
import com.dunk.espelhoponto.exception.RegraNegocioException;
import com.dunk.espelhoponto.infra.security.service.TokenService;
import com.dunk.espelhoponto.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository repository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((Usuario) auth.getPrincipal());

        Usuario usuario = (Usuario) auth.getPrincipal();

        return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getNome()));
    }

}