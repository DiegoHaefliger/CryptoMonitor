package com.haefliger.cryptomonitor.orchestrator;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.service.RedisService;
import com.haefliger.cryptomonitor.strategy.AnaliseEstrategia;
import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import com.haefliger.cryptomonitor.strategy.dto.SimboloMonitorado;
import io.micrometer.common.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
public class SimboloMonitoradoFactory {

    private final AnaliseEstrategia rsi;
    private final AnaliseEstrategia mm;
    private final RedisService redisService;

    @Autowired
    private OrquestradorAnalisesService orquestradorAnalisesService;

    public SimboloMonitoradoFactory(@Qualifier("estrategiaRSI") AnaliseEstrategia rsi,
                                    @Qualifier("estrategiaMediaMovel") AnaliseEstrategia mm,
                                    RedisService redisService) {
        this.rsi = rsi;
        this.mm = mm;
        this.redisService = redisService;
    }

    public List<SimboloMonitorado> criarSimbolosMonitorados(@NonNull List<PrecoSimbolo> historicoPrecos,
                                                            @NonNull String simboloIntervalo) {

        // monta a lista de estratégias disponíveis
        List<AnaliseEstrategia> estrategiasDisponiveis = criarEstrategiasDisponiveis();
        Map<String, Set<String>> estrategiasPorSimbolo = buscarEstrategiasPorSimbolo(simboloIntervalo);

        Set<String> estrategiasParaSimbolo = estrategiasPorSimbolo.getOrDefault(simboloIntervalo, Set.of());
        List<AnaliseEstrategia> estrategiasFiltradas = estrategiasDisponiveis.stream()
                .filter(e -> estrategiasParaSimbolo.contains(e.getNome()))
                .toList();
        SimboloMonitorado simboloMonitorado = SimboloMonitorado.builder()
                .simbolo(simboloIntervalo)
                .estrategias(estrategiasFiltradas)
                .build();

        if (!simboloMonitorado.getEstrategias().isEmpty()) {
            orquestradorAnalisesService.analisarMonitorados(historicoPrecos, List.of(simboloMonitorado));
        }

        return List.of(simboloMonitorado);
    }

    private List<AnaliseEstrategia> criarEstrategiasDisponiveis() {
        return List.of(rsi, mm);
    }

    private Map<String, Set<String>> buscarEstrategiasPorSimbolo(String simboloIntervalo) {
        String simbolo = simboloIntervalo.split("-")[0];
        String intervalo = simboloIntervalo.split("-")[1];
        List<Estrategia> estrategias = redisService.buscarEstrategiasAtivasRedis();
        if (estrategias == null || estrategias.isEmpty()) {
            log.warn("Nenhuma estratégia ativa encontrada no Redis ou no banco de dados.");
            return Map.of();
        }

        // A chave do map deve ser o simboloIntervalo
       return estrategias.stream()
                .filter(estrategia -> estrategia.getSimbolo().equals(simbolo) && estrategia.getIntervalo().equals(intervalo))
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

}
