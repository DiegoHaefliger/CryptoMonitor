package com.haefliger.cryptomonitor.orchestrator.impl;


import com.haefliger.cryptomonitor.orchestrator.OrquestradorAnalisesService;
import com.haefliger.cryptomonitor.strategy.AnaliseEstrategia;
import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import com.haefliger.cryptomonitor.strategy.dto.SimboloMonitorado;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Author diego-haefliger
 * Date 25/06/25
 */

@Service
@AllArgsConstructor
@Slf4j
public class OrquestradorAnalisesServiceImpl implements OrquestradorAnalisesService {

    private final Map<String, SimboloMonitorado> simbolosMonitorados;

    public void analisarTudo(Map<String, List<PrecoSimbolo>> historicoPrecosPorSimbolo) {
        for (String simbolo : simbolosMonitorados.keySet()) {
            SimboloMonitorado monitorado = simbolosMonitorados.get(simbolo);
            List<PrecoSimbolo> historico = historicoPrecosPorSimbolo.get(simbolo);
            if(historico != null) {
                for (AnaliseEstrategia estrategia : monitorado.getEstrategias()) {
                    estrategia.analisar(historico, simbolo);
                }
            }
        }
    }

    public void analisarMonitorados(List<PrecoSimbolo> historicoPrecos, List<SimboloMonitorado> simbolosMonitorados) {
        // TODO: verificar se está enviando o histórico de preços correto para cada estratégia

        for (SimboloMonitorado monitorado : simbolosMonitorados) {
            if (historicoPrecos != null) {
                for (AnaliseEstrategia estrategia : monitorado.getEstrategias()) {
                    estrategia.analisar(historicoPrecos, monitorado.getSimbolo());
                }
            }
        }
    }
}
