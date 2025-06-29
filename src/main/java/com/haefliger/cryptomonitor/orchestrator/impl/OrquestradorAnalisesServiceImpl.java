package com.haefliger.cryptomonitor.orchestrator.impl;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.orchestrator.OrquestradorAnalisesService;
import com.haefliger.cryptomonitor.strategy.AnaliseEstrategia;
import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import com.haefliger.cryptomonitor.strategy.dto.SimboloMonitorado;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author diego-haefliger
 * Date 25/06/25
 */

@Service
@AllArgsConstructor
@Slf4j
public class OrquestradorAnalisesServiceImpl implements OrquestradorAnalisesService {

    public void analisarMonitorados(List<PrecoSimbolo> historicoPrecos, List<SimboloMonitorado> simbolosMonitorados, List<Estrategia> estrategias) {
        for (SimboloMonitorado monitorado : simbolosMonitorados) {
            if (historicoPrecos != null) {
                for (AnaliseEstrategia analise : monitorado.getEstrategias()) {
                    analise.analisar(historicoPrecos, monitorado.getSimbolo(), estrategias);
                }
            }
        }
    }
}
