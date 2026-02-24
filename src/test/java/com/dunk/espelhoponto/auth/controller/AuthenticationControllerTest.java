package com.dunk.espelhoponto.auth.controller;

import com.dunk.espelhoponto.auth.dto.AuthenticationDTO;
import com.dunk.espelhoponto.auth.dto.LoginResponseDTO;
import com.dunk.espelhoponto.auth.dto.RegisterDTO;
import com.dunk.espelhoponto.dto.RegistroUsuarioResponseDTO;
import com.dunk.espelhoponto.entity.Usuario;
import com.dunk.espelhoponto.enums.RegraUsuario;
import com.dunk.espelhoponto.exception.RegraNegocioException;
import com.dunk.espelhoponto.infra.security.service.TokenService;
import com.dunk.espelhoponto.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UsuarioRepository repository;
    @Mock
    private TokenService tokenService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationController controller;

    @Test
    @DisplayName("Deve realizar login e retornar token")
    void deveLogarComSucesso() {

        AuthenticationDTO dto = new AuthenticationDTO("vinicius@email.com", "123");
        Authentication authMock = mock(Authentication.class);
        Usuario usuarioMock = new Usuario(UUID.randomUUID(), "vinicius@email.com", "cripto", "Teste", RegraUsuario.USER);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(usuarioMock);
        when(tokenService.generateToken(usuarioMock)).thenReturn("token-jwt-valido");

        ResponseEntity<LoginResponseDTO> response = controller.login(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token-jwt-valido", response.getBody().token());
    }
}