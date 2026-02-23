package com.dunk.espelhoponto.controller;

import com.dunk.espelhoponto.auth.dto.RegisterDTO;
import com.dunk.espelhoponto.dto.RegistroUsuarioResponseDTO;
import com.dunk.espelhoponto.entity.Usuario;
import com.dunk.espelhoponto.exception.RegraNegocioException;
import com.dunk.espelhoponto.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<RegistroUsuarioResponseDTO> criarUsario(@RequestBody @Valid RegisterDTO data) {

        if (this.repository.existsByLogin(data.login())) {
            throw new RegraNegocioException("O login " + data.login() + " já existe! ");
        }

        String encryptedPassword = passwordEncoder.encode(data.senha());

        Usuario newUser = new Usuario(
                data.login(),
                encryptedPassword,
                data.nome(),
                data.regra()
        );

        this.repository.save(newUser);

        var response = new RegistroUsuarioResponseDTO(
                newUser.getLogin(),
                newUser.getRegra(),
                "Usuário criado com sucesso pelo Administrador! ",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
