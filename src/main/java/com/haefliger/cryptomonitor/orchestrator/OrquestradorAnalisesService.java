package com.haefliger.cryptomonitor.orchestrator;


import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import com.haefliger.cryptomonitor.strategy.dto.SimboloMonitorado;

import java.util.List;
import java.util.Map;

/**
 * Author diego-haefliger
 * Date 6/25/25
 */
public interface OrquestradorAnalisesService {

    void analisarTudo(Map<String, List<PrecoSimbolo>> historicoPrecosPorSimbolo);

    void analisarMonitorados(List<PrecoSimbolo> historicoPrecos, List<SimboloMonitorado> simbolosParaMonitorar);

}
