package com.haefliger.cryptomonitor.strategy;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Author diego-haefliger
 * Date 25/06/25
 */

@Component
@Slf4j
public class EstrategiaMediaMovel implements AnaliseEstrategia {

    @Override
    public void analisar(List<PrecoSimboloDomain> historicoPreco, String simbolo, List<Estrategia> estrategias) {
        // TODO: Implementação do cálculo do RSI
        log.info("Analisando {} com estratégia de Média Móvel", simbolo);
    }

    @Override
    public String getNome() {
        return TipoIndicadorEnum.MEDIA_MOVEL.name();
    }
}
