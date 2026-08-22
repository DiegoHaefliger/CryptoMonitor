package com.haefliger.cryptomonitor.strategy;

import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EstrategiaMediaMovel implements AnaliseEstrategia {

    private static final Logger log = LoggerFactory.getLogger(EstrategiaMediaMovel.class);

    @Override
    public void analisar(
            List<PrecoSimboloDomain> historicoPreco, String simbolo, List<Estrategia> estrategias) {
        // TODO: Implementação do cálculo da média móvel
        log.info("Analisando {} com estratégia de Média Móvel", simbolo);
    }

    @Override
    public String getNome() {
        return TipoIndicadorEnum.MEDIA_MOVEL.name();
    }
}
