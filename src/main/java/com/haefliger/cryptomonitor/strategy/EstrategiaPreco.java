package com.haefliger.cryptomonitor.strategy;


import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Author diego-haefliger
 * Date 27/06/25
 */

@Component
@Slf4j
public class EstrategiaPreco implements AnaliseEstrategia {

    @Override
    public void analisar(List<PrecoSimbolo> historicoPreco, String simbolo) {
        // TODO: Implementação estratégia de preço
        log.info("Analisando {} com estratégia de Preco", simbolo);
    }

    @Override
    public String getNome() {
        return TipoIndicadorEnum.PRECO.name();
    }
}
