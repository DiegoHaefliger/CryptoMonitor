package com.haefliger.cryptomonitor.orchestrator;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import com.haefliger.cryptomonitor.strategy.dto.SimboloMonitorado;

import java.util.List;

/**
 * Author diego-haefliger
 * Date 6/25/25
 */
public interface OrquestradorAnalisesService {

    void analisarMonitorados(List<PrecoSimbolo> historicoPrecos, List<SimboloMonitorado> simbolosParaMonitorar, List<Estrategia> estrategias);

}
