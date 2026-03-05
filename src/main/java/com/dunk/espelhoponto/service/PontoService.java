package com.dunk.espelhoponto.service;

import com.dunk.espelhoponto.dto.DiaJornadaDTO;
import com.dunk.espelhoponto.dto.HistoricoDiarioDTO;
import com.dunk.espelhoponto.dto.RegistroPontoResponseDTO;
import com.dunk.espelhoponto.entity.Ponto;
import com.dunk.espelhoponto.entity.Usuario;
import com.dunk.espelhoponto.dto.SaldoHorasDTO;
import com.dunk.espelhoponto.enums.TipoRegistro;
import com.dunk.espelhoponto.exception.RegraNegocioException;
import com.dunk.espelhoponto.repository.PontoRepository;
import com.dunk.espelhoponto.strategy.CalculoAdicionalNoturno;
import com.dunk.espelhoponto.strategy.CalculoFimDeSemana;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PontoService {

    private final PontoRepository repository;

    private final CalculoAdicionalNoturno estrategiaNoturna;
    private final CalculoFimDeSemana estrategiaFds;

    public RegistroPontoResponseDTO registrar() {

        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LocalDateTime agora = LocalDateTime.now();

        var ultimoPontoOpt = repository.findTopByUsuarioOrderByDataHoraDesc(usuarioLogado);
        String aviso = null;

        if (ultimoPontoOpt.isPresent()) {
            Ponto ultimoPonto = ultimoPontoOpt.get();
            long minutosDiferenca = java.time.temporal.ChronoUnit.MINUTES.between(ultimoPonto.getDataHora(), agora);

            if (minutosDiferenca < 5) {
                throw new RegraNegocioException("Espere 5 minutos! Último registro foi há " + minutosDiferenca + " min.");
            }
        }

        LocalDateTime inicioDoDia = agora.toLocalDate().atStartOfDay();
        LocalDateTime fimDoDia = agora.toLocalDate().atTime(java.time.LocalTime.MAX);

        long qtdBatidasHoje = repository.countByUsuarioAndDataHoraBetween(usuarioLogado, inicioDoDia, fimDoDia);

        if (qtdBatidasHoje >= 6) {
            throw new RegraNegocioException("Limite diário atingido! Você já realizou os 6 registros permitidos hoje.");
        }

        TipoRegistro tipoCalculado = (qtdBatidasHoje % 2 == 0) ? TipoRegistro.ENTRADA : TipoRegistro.SAIDA;

        if (tipoCalculado == TipoRegistro.ENTRADA && ultimoPontoOpt.isPresent()) {
            Ponto ultimoPonto = ultimoPontoOpt.get();
            if (ultimoPonto.getTipo() == TipoRegistro.SAIDA) {
                long minutosDiferenca = java.time.temporal.ChronoUnit.MINUTES.between(ultimoPonto.getDataHora(), agora);
                if (minutosDiferenca < 60) {
                    aviso = "ALERTA: Intervalo de descanso inferior a 1 hora (" + minutosDiferenca + " min). Isso pode gerar horas extras ou infração.";
                }
            }
        }

        Ponto ponto = Ponto.builder()
                .usuario(usuarioLogado)
                .dataHora(agora)
                .tipo(tipoCalculado)
                .build();

        repository.save(ponto);

        return new RegistroPontoResponseDTO("Ponto de " + tipoCalculado + " registrado com sucesso!", aviso, ponto.getTipo(), ponto.getDataHora());
    }

    public SaldoHorasDTO calcularBancoHoras(Usuario usuario, LocalDate inicio, LocalDate fim) {
        List<Ponto> pontos = repository.findByUsuarioAndDataHoraBetweenOrderByDataHoraAsc(usuario, inicio.atStartOfDay(), fim.atTime(23, 59, 59));

        long minutosTrabalhados = 0;
        List<String> avisos = new ArrayList<>();

        LocalDate diaAtual = null;
        List<Ponto> pontosDoDia = new ArrayList<>();

        for (Ponto ponto : pontos) {
            LocalDate dataPonto = ponto.getDataHora().toLocalDate();

            if (diaAtual != null && !dataPonto.equals(diaAtual)) {
                minutosTrabalhados += processarDia(pontosDoDia, avisos);
                pontosDoDia.clear();
            }

            diaAtual = dataPonto;
            pontosDoDia.add(ponto);
        }
        if (!pontosDoDia.isEmpty()) {
            minutosTrabalhados += processarDia(pontosDoDia, avisos);
        }

        long minutosEsperados = (long) (pontos.stream().map(p -> p.getDataHora().toLocalDate()).distinct().count() * 480); // 8h por dia trabalhado

        long saldo = minutosTrabalhados - minutosEsperados;
        String sinal = saldo >= 0 ? "+" : "-";
        String saldoFormatado = String.format("%s%02d:%02d", sinal, Math.abs(saldo) / 60, Math.abs(saldo) % 60);

        return new SaldoHorasDTO(usuario.getUsername(), saldoFormatado, minutosTrabalhados, minutosEsperados, avisos);
    }

    public List<DiaJornadaDTO> obterJornadaUltimosSeteDias() {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LocalDate hoje = LocalDate.now();
        LocalDate seteDiasAtras = hoje.minusDays(6);

        List<Ponto> pontos = repository.findByUsuarioAndDataHoraBetweenOrderByDataHoraAsc(
                usuario,
                seteDiasAtras.atStartOfDay(),
                hoje.atTime(LocalTime.MAX)
        );

        Map<LocalDate, List<Ponto>> pontosPorDia = pontos.stream()
                .collect(Collectors.groupingBy(p -> p.getDataHora().toLocalDate()));

        List<DiaJornadaDTO> grafico = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE", new Locale("pt", "BR"));

        for (int i = 0; i <= 6; i++) {
            LocalDate dataAtual = seteDiasAtras.plusDays(i);
            List<Ponto> batidasDoDia = pontosPorDia.getOrDefault(dataAtual, List.of());

            long minutosTrabalhados = calcularMinutosDoDia(batidasDoDia);

            double horas = Math.round((minutosTrabalhados / 60.0) * 10.0) / 10.0;

            String nomeDia = dataAtual.format(formatter).replace(".", "");
            nomeDia = nomeDia.substring(0, 1).toUpperCase() + nomeDia.substring(1);

            grafico.add(new DiaJornadaDTO(nomeDia, horas));
        }

        return grafico;
    }

    public Page<HistoricoDiarioDTO> gerarHistoricoPaginado(Usuario usuario, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {

        LocalDateTime inicioDia = dataInicio.atStartOfDay();
        LocalDateTime fimDia = dataFim.atTime(23, 59, 59);

        List<Ponto> pontosBrutos = repository.findByUsuarioAndDataHoraBetweenOrderByDataHoraAsc(usuario, inicioDia, fimDia);

        Map<LocalDate, List<Ponto>> pontosPorDia = pontosBrutos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getDataHora().toLocalDate(),
                        () -> new TreeMap<>(Collections.reverseOrder()),
                        Collectors.toList()
                ));

        List<HistoricoDiarioDTO> historicoCompleto = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Map.Entry<LocalDate, List<Ponto>> entry : pontosPorDia.entrySet()) {
            LocalDate data = entry.getKey();
            List<Ponto> batidasDoDia = entry.getValue();

            batidasDoDia.sort(Comparator.comparing(Ponto::getDataHora));

            List<String> marcacoesFormatadas = new ArrayList<>();
            long minutosTrabalhados = 0;

            for (int i = 0; i < batidasDoDia.size(); i++) {
                Ponto batidaAtual = batidasDoDia.get(i);
                marcacoesFormatadas.add(batidaAtual.getDataHora().format(timeFormatter));

                if (i % 2 != 0) {
                    Ponto entradaAnterior = batidasDoDia.get(i - 1);
                    minutosTrabalhados += Duration.between(entradaAnterior.getDataHora(), batidaAtual.getDataHora()).toMinutes();
                }
            }

            String status = batidasDoDia.size() % 2 == 0 ? "NORMAL" : "INCOMPLETO";

            long horas = minutosTrabalhados / 60;
            long minutosRestantes = minutosTrabalhados % 60;
            String horasFormatadas = String.format("%02dh%02d", horas, minutosRestantes);

            historicoCompleto.add(new HistoricoDiarioDTO(
                    data,
                    marcacoesFormatadas,
                    minutosTrabalhados,
                    horasFormatadas,
                    status
            ));
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), historicoCompleto.size());

        List<HistoricoDiarioDTO> conteudoPaginado = start <= end
                ? historicoCompleto.subList(start, end)
                : new ArrayList<>();

        return new PageImpl<>(conteudoPaginado, pageable, historicoCompleto.size());
    }


    private long calcularMinutosDoDia(List<Ponto> batidas) {
        long totalMinutos = 0;
        Ponto entradaTemporaria = null;

        for (Ponto p : batidas) {
            if (p.getTipo() == TipoRegistro.ENTRADA) {
                entradaTemporaria = p;
            } else if (p.getTipo() == TipoRegistro.SAIDA && entradaTemporaria != null) {
                totalMinutos += java.time.temporal.ChronoUnit.MINUTES.between(entradaTemporaria.getDataHora(), p.getDataHora());
                entradaTemporaria = null;
            }
        }
        return totalMinutos;
    }

    private long processarDia(List<Ponto> pontos, List<String> avisos) {
        long minutosNoDia = 0;
        boolean teveAlmoco = false;

        for (int i = 0; i < pontos.size() - 1; i++) {
            Ponto atual = pontos.get(i);
            Ponto proximo = pontos.get(i + 1);

            if (atual.getTipo() == TipoRegistro.ENTRADA && proximo.getTipo() == TipoRegistro.SAIDA) {

                if (isFimDeSemana(atual.getDataHora())) {
                    minutosNoDia += estrategiaFds.calcular(atual.getDataHora(), proximo.getDataHora());
                    avisos.add("Dia " + atual.getDataHora().toLocalDate() + ": Fim de semana contabilizado (100%).");
                } else {
                    minutosNoDia += estrategiaNoturna.calcular(atual.getDataHora(), proximo.getDataHora());
                }
            }

            if (atual.getTipo() == TipoRegistro.SAIDA && proximo.getTipo() == TipoRegistro.ENTRADA) {
                long minutosIntervalo = Duration.between(atual.getDataHora(), proximo.getDataHora()).toMinutes();

                if (minutosIntervalo < 60) {
                    avisos.add("INFRAÇÃO: Intervalo interjornada menor que 1h no dia " + atual.getDataHora().toLocalDate() + " (" + minutosIntervalo + " min).");
                }
                teveAlmoco = true;
            }
        }

        if (pontos.size() > 2 && !teveAlmoco) {
            avisos.add("ALERTA: Possível falta de registro de intervalo no dia " + pontos.get(0).getDataHora().toLocalDate());
        }

        return minutosNoDia;
    }

    private boolean isFimDeSemana(LocalDateTime data) {
        DayOfWeek d = data.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }
}