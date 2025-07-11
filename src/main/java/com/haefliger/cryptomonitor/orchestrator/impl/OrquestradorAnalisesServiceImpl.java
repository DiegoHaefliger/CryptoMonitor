package com.haefliger.cryptomonitor.orchestrator.impl;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.orchestrator.OrquestradorAnalisesService;
import com.haefliger.cryptomonitor.strategy.AnaliseEstrategia;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import com.haefliger.cryptomonitor.strategy.domain.SimboloMonitoradoDomain;
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

    public void analisarMonitorados(List<PrecoSimboloDomain> historicoPrecos, List<SimboloMonitoradoDomain> simbolosMonitorados, List<Estrategia> estrategias) {
        for (SimboloMonitoradoDomain monitorado : simbolosMonitorados) {
            if (historicoPrecos != null) {
                for (AnaliseEstrategia analise : monitorado.getEstrategias()) {
                    analise.analisar(historicoPrecos, monitorado.getSimbolo(), estrategias);
                }
            }
        }
    }
}
