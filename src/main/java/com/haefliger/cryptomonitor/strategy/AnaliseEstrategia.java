package com.haefliger.cryptomonitor.strategy;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;

import java.util.List;

/**
 * Author diego-haefliger
 * Date 25/06/25
 */
public interface AnaliseEstrategia {

    void analisar(List<PrecoSimbolo> historicoPreco, String simbolo, List<Estrategia> estrategias);
    String getNome();

}
