package com.dunk.espelhoponto.repository;

import com.dunk.espelhoponto.entity.Ponto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PontoRepository extends JpaRepository<Ponto, Long> {

    /*
        Busca pontos de um funcionário em um intervalo de datas, ordenado por data
    */
    List<Ponto> findByNomeFuncionarioAndDataHoraBetweenOrderByDataHoraAsc(
            String nomeFuncionario, LocalDateTime inicio, LocalDateTime fim);

    Optional<Ponto> findTopByNomeFuncionarioOrderByDataHoraDesc(String nomeFuncionario);
}
