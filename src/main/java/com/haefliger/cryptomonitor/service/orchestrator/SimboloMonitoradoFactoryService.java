package com.haefliger.cryptomonitor.service.orchestrator;

import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.service.RedisService;
import com.haefliger.cryptomonitor.strategy.AnaliseEstrategia;
import com.haefliger.cryptomonitor.strategy.EstrategiaMediaMovel;
import com.haefliger.cryptomonitor.strategy.EstrategiaPreco;
import com.haefliger.cryptomonitor.strategy.EstrategiaRSI;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import com.haefliger.cryptomonitor.strategy.domain.SimboloMonitoradoDomain;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SimboloMonitoradoFactoryService {

    private static final Logger log =
            LoggerFactory.getLogger(SimboloMonitoradoFactoryService.class);

    private final List<AnaliseEstrategia> estrategiasDisponiveis;
    private final RedisService redisService;
    private final OrquestradorAnalisesService orquestradorAnalisesService;

    SimboloMonitoradoFactoryService(
            EstrategiaRSI rsi,
            EstrategiaMediaMovel mm,
            EstrategiaPreco preco,
            RedisService redisService,
            OrquestradorAnalisesService orquestradorAnalisesService) {
        this.estrategiasDisponiveis = List.of(rsi, mm, preco);
        this.redisService = redisService;
        this.orquestradorAnalisesService = orquestradorAnalisesService;
    }

    public List<SimboloMonitoradoDomain> criarSimbolosMonitorados(
            List<PrecoSimboloDomain> historicoPrecos, String simboloIntervalo) {
        List<Estrategia> estrategias = buscarEstrategiasAtivasRedis(simboloIntervalo);
        Map<String, Set<String>> estrategiasPorSimbolo =
                buscarEstrategiasPorSimbolo(simboloIntervalo, estrategias);
        Set<String> estrategiasParaSimbolo =
                estrategiasPorSimbolo.getOrDefault(simboloIntervalo, Set.of());

        List<AnaliseEstrategia> estrategiasFiltradas =
                estrategiasDisponiveis.stream()
                        .filter(e -> estrategiasParaSimbolo.contains(e.getNome()))
                        .toList();

        SimboloMonitoradoDomain simboloMonitoradoDomain =
                new SimboloMonitoradoDomain(simboloIntervalo, estrategiasFiltradas);

        if (!simboloMonitoradoDomain.estrategias().isEmpty()) {
            orquestradorAnalisesService.analisarMonitorados(
                    historicoPrecos, List.of(simboloMonitoradoDomain), estrategias);
        }

        return List.of(simboloMonitoradoDomain);
    }

    private Map<String, Set<String>> buscarEstrategiasPorSimbolo(
            String simboloIntervalo, List<Estrategia> estrategias) {
        return estrategias.stream()
                .collect(
                        Collectors.toMap(
                                e -> simboloIntervalo,
                                estrategia ->
                                        estrategia.getCondicoes().stream()
                                                .map(cond -> cond.getTipoIndicador().name())
                                                .collect(Collectors.toSet()),
                                (set1, set2) -> {
                                    set1.addAll(set2);
                                    return set1;
                                }));
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
                .filter(
                        estrategia ->
                                estrategia.getSimbolo().equals(simbolo)
                                        && estrategia.getIntervalo().equals(intervalo))
                .toList();
    }
}
