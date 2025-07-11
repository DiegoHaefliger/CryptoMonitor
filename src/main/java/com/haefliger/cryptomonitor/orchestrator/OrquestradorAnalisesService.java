package com.haefliger.cryptomonitor.orchestrator;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import com.haefliger.cryptomonitor.strategy.domain.SimboloMonitoradoDomain;

import java.util.List;

/**
 * Author diego-haefliger
 * Date 6/25/25
 */
public interface OrquestradorAnalisesService {

    void analisarMonitorados(List<PrecoSimboloDomain> historicoPrecos, List<SimboloMonitoradoDomain> simbolosParaMonitorar, List<Estrategia> estrategias);

}
