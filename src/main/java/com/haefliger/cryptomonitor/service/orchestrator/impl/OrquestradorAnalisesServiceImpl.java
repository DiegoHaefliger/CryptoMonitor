package com.haefliger.cryptomonitor.service.orchestrator.impl;

import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.service.orchestrator.OrquestradorAnalisesService;
import com.haefliger.cryptomonitor.strategy.AnaliseEstrategia;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import com.haefliger.cryptomonitor.strategy.domain.SimboloMonitoradoDomain;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class OrquestradorAnalisesServiceImpl implements OrquestradorAnalisesService {

    @Override
    public void analisarMonitorados(List<PrecoSimboloDomain> historicoPrecos,
                                    List<SimboloMonitoradoDomain> simbolosMonitorados,
                                    List<Estrategia> estrategias) {
        for (SimboloMonitoradoDomain monitorado : simbolosMonitorados) {
            if (historicoPrecos != null) {
                for (AnaliseEstrategia analise : monitorado.estrategias()) {
                    analise.analisar(historicoPrecos, monitorado.simbolo(), estrategias);
                }
            }
        }
    }
}
