package com.dunk.espelhoponto.repository;

import com.dunk.espelhoponto.entity.Usuario;
import com.dunk.espelhoponto.entity.Ponto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PontoRepository extends JpaRepository<Ponto, Long> {


    List<Ponto> findByUsuarioAndDataHoraBetweenOrderByDataHoraAsc(
            Usuario usuario, LocalDateTime inicio, LocalDateTime fim);

    Optional<Ponto> findTopByUsuarioOrderByDataHoraDesc(Usuario usuario);
}