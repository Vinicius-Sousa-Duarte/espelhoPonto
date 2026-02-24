package com.dunk.espelhoponto.controller;

import com.dunk.espelhoponto.auth.dto.RegisterDTO;
import com.dunk.espelhoponto.entity.Usuario;
import com.dunk.espelhoponto.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarUsuario(@RequestBody @Valid RegisterDTO data) {

        if (this.repository.findByLogin(data.login()) != null) {
            return ResponseEntity.badRequest().body("Login já existe");
        }

        String encryptedPassword = passwordEncoder.encode(data.senha());

        Usuario newUser = new Usuario(
                data.login(),
                encryptedPassword,
                data.nome(),
                data.regra()
        );

        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }
}