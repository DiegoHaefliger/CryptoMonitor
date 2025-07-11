package com.haefliger.cryptomonitor.orchestrator;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.service.RedisService;
import com.haefliger.cryptomonitor.strategy.AnaliseEstrategia;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import com.haefliger.cryptomonitor.strategy.domain.SimboloMonitoradoDomain;
import io.micrometer.common.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author diego-haefliger
 * Date 25/06/25
 */

@Service
@Slf4j
public class SimboloMonitoradoFactoryService {

    private final AnaliseEstrategia rsi;
    private final AnaliseEstrategia mm;
    private final AnaliseEstrategia preco;
    private final RedisService redisService;

    private final OrquestradorAnalisesService orquestradorAnalisesService;

    public SimboloMonitoradoFactoryService(@Qualifier("estrategiaRSI") AnaliseEstrategia rsi,
                                           @Qualifier("estrategiaMediaMovel") AnaliseEstrategia mm,
                                           @Qualifier("estrategiaPreco") AnaliseEstrategia preco,
                                           RedisService redisService, OrquestradorAnalisesService orquestradorAnalisesService) {
        this.rsi = rsi;
        this.mm = mm;
        this.preco = preco;
        this.redisService = redisService;
        this.orquestradorAnalisesService = orquestradorAnalisesService;
    }

    private List<AnaliseEstrategia> criarEstrategiasDisponiveis() {
        return List.of(rsi, mm, preco);
    }

    public List<SimboloMonitoradoDomain> criarSimbolosMonitorados(@NonNull List<PrecoSimboloDomain> historicoPrecos,
                                                                  @NonNull String simboloIntervalo) {

        // monta a lista de estratégias disponíveis
        List<AnaliseEstrategia> estrategiasDisponiveis = criarEstrategiasDisponiveis();
        List<Estrategia> estrategias = buscarEstrategiasAtivasRedis(simboloIntervalo);
        Map<String, Set<String>> estrategiasPorSimbolo = buscarEstrategiasPorSimbolo(simboloIntervalo, estrategias);
        Set<String> estrategiasParaSimbolo = estrategiasPorSimbolo.getOrDefault(simboloIntervalo, Set.of());

        List<AnaliseEstrategia> estrategiasFiltradas = estrategiasDisponiveis.stream()
                .filter(e -> estrategiasParaSimbolo.contains(e.getNome()))
                .toList();

        SimboloMonitoradoDomain simboloMonitoradoDomain = SimboloMonitoradoDomain.builder()
                .simbolo(simboloIntervalo)
                .estrategias(estrategiasFiltradas)
                .build();

        if (!simboloMonitoradoDomain.getEstrategias().isEmpty()) {
            orquestradorAnalisesService.analisarMonitorados(historicoPrecos, List.of(simboloMonitoradoDomain), estrategias);
        }

        return List.of(simboloMonitoradoDomain);
    }

    private Map<String, Set<String>> buscarEstrategiasPorSimbolo(String simboloIntervalo, List<Estrategia> estrategias) {
        return estrategias.stream()
                .collect(Collectors.toMap(
                        e -> simboloIntervalo,
                        estrategia -> estrategia.getCondicoes().stream()
                                .map(cond -> cond.getTipoIndicador().name())
                                .collect(Collectors.toSet()),
                        (set1, set2) -> {
                            set1.addAll(set2);
                            return set1;
                        }
                ));
    }

    private List<Estrategia> buscarEstrategiasAtivasRedis(String simboloIntervalo) {
        String simbolo = simboloIntervalo.split("-")[0];
        String intervalo = simboloIntervalo.split("-")[1];
        List<Estrategia> estrategias = redisService.buscarEstrategiasAtivasRedis();

        if (estrategias == null || estrategias.isEmpty()) {
            log.warn("Nenhuma estratégia ativa encontrada no Redis ou no banco de dados.");
            return Collections.emptyList();
        }

        return estrategias.stream()
                .filter(estrategia -> estrategia.getSimbolo().equals(simbolo) && estrategia.getIntervalo().equals(intervalo))
                .toList();
    }
}
