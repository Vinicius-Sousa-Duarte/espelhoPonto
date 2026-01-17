package com.dunk.espelhoponto.service;

import com.dunk.espelhoponto.dto.NovoRegistroDTO;
import com.dunk.espelhoponto.dto.SaldoHorasDTO;
import com.dunk.espelhoponto.entity.Ponto;
import com.dunk.espelhoponto.enums.TipoRegistro;
import com.dunk.espelhoponto.exception.RegraNegocioException; // Certifique-se de criar esta classe
import com.dunk.espelhoponto.repository.PontoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PontoService {

    private static final long JORNADA_DIARIA_MINUTOS = 8 * 60; // 8 Horas
    private final PontoRepository repository;

    @Transactional
    public void registrar(NovoRegistroDTO dto) {
        var ultimoRegistroOpt = repository
                .findTopByNomeFuncionarioOrderByDataHoraDesc(dto.nomeFuncionario());

        if (ultimoRegistroOpt.isPresent()) {
            Ponto ultimo = ultimoRegistroOpt.get();
            if (ultimo.getTipo() == dto.tipo()) {
                throw new RegraNegocioException("Sequência inválida: O último registro já foi do tipo " + ultimo.getTipo());
            }
        } else {
            if (dto.tipo() == TipoRegistro.SAIDA) {
                throw new RegraNegocioException("Não é possível registrar SAÍDA sem haver uma ENTRADA anterior.");
            }
        }

        Ponto ponto = Ponto.builder()
                .nomeFuncionario(dto.nomeFuncionario())
                .dataHora(LocalDateTime.now())
                .tipo(dto.tipo())
                .build();

        repository.save(ponto);
    }

    public SaldoHorasDTO calcularBancoHoras(String funcionario, LocalDate inicio, LocalDate fim) {
        List<Ponto> pontos = repository.findByNomeFuncionarioAndDataHoraBetweenOrderByDataHoraAsc(
                funcionario, inicio.atStartOfDay(), fim.atTime(LocalTime.MAX));

        Map<LocalDate, List<Ponto>> pontosPorDia = pontos.stream()
                .collect(Collectors.groupingBy(p -> p.getDataHora().toLocalDate()));

        long saldoGeralMinutos = 0;
        long totalTrabalhado = 0;
        long totalEsperado = pontosPorDia.size() * JORNADA_DIARIA_MINUTOS;

        for (List<Ponto> registrosDoDia : pontosPorDia.values()) {
            long minutosNoDia = calcularMinutosTrabalhadosNoDia(registrosDoDia);
            totalTrabalhado += minutosNoDia;
            saldoGeralMinutos += (minutosNoDia - JORNADA_DIARIA_MINUTOS);
        }

        return new SaldoHorasDTO(
                funcionario,
                formatarSaldo(saldoGeralMinutos),
                totalTrabalhado,
                totalEsperado
        );
    }

    private long calcularMinutosTrabalhadosNoDia(List<Ponto> pontos) {
        long minutos = 0;

        for (int i = 0; i < pontos.size() - 1; i++) {
            Ponto atual = pontos.get(i);
            Ponto proximo = pontos.get(i + 1);

            if (atual.getTipo() == TipoRegistro.ENTRADA && proximo.getTipo() == TipoRegistro.SAIDA) {
                minutos += Duration.between(atual.getDataHora(), proximo.getDataHora()).toMinutes();
                i++;
            }
        }
        return minutos;
    }

    private String formatarSaldo(long minutos) {
        long absMinutos = Math.abs(minutos);
        long horas = absMinutos / 60;
        long mins = absMinutos % 60;
        String sinal = minutos >= 0 ? "+" : "-";
        return String.format("%s%02d:%02d", sinal, horas, mins);
    }
}