package com.haefliger.cryptomonitor.strategy;


import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Author diego-haefliger
 * Date 25/06/25
 */

@Component
@Slf4j
public class EstrategiaRSI implements AnaliseEstrategia {

    @Override
    public void analisar(List<PrecoSimbolo> historicoPreco, String simbolo) {
        // TODO: Implementação do cálculo do RSI
        log.info("Analisando {} com estratégia de RSI", simbolo);
    }

    @Override
    public String getNome() {
        return TipoIndicadorEnum.RSI.name();
    }

}
