package com.dunk.espelhoponto.repository;

import com.dunk.espelhoponto.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    UserDetails findByLogin(String login);
    boolean existsByLogin(String login);
}