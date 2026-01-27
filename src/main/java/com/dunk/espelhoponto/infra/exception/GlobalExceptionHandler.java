package com.dunk.espelhoponto.infra.exception;

import com.dunk.espelhoponto.exception.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RegraNegocioException.class)
  public ResponseEntity<Map<String, Object>> handleRegraNegocio(RegraNegocioException ex) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
            "erro", "Regra de Negócio Violada",
            "mensagem", ex.getMessage(),
            "timestamp", LocalDateTime.now()
    ));
  }
}