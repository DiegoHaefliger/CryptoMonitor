package com.haefliger.cryptomonitor.strategy;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Author diego-haefliger
 * Date 27/06/25
 */

@Component
@Slf4j
public class EstrategiaPreco implements AnaliseEstrategia {

    @Override
    public void analisar(List<PrecoSimbolo> historicoPreco, String simbolo, List<Estrategia> estrategias) {
        BigDecimal precoAtual = historicoPreco.get(historicoPreco.size() - 1).getPrice();
        BigDecimal precoAnterior = historicoPreco.get(historicoPreco.size() - 2).getPrice();

        for (Estrategia estrategia : estrategias) {
            BigDecimal precoAlvo = estrategia.getCondicoes().get(0).getValor();
            boolean isPrecoSubiu = ((precoAtual.compareTo(precoAlvo) > 0) && (precoAnterior.compareTo(precoAlvo) < 0));
            boolean isPrecoDesceu = ((precoAtual.compareTo(precoAlvo) < 0) && (precoAnterior.compareTo(precoAlvo) > 0));
            if (isPrecoSubiu || isPrecoDesceu) {
                log.info("Alarme acionado para o símbolo: {} com preço de alarme: {}", simbolo, precoAlvo);
            }
        }
        log.info("Analisando {} com estratégia de Preco", simbolo);
    }

    @Override
    public String getNome() {
        return TipoIndicadorEnum.PRECO.name();
    }
}
