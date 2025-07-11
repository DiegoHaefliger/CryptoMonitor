package com.haefliger.cryptomonitor.strategy;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.service.KafkaService;
import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private KafkaService kafkaService;

    @Override
    public void analisar(List<PrecoSimbolo> historicoPreco, String simboloIntervalo, List<Estrategia> estrategias) {
        if (historicoPreco == null || historicoPreco.size() < 2) {
            log.warn("Histórico de preço insuficiente para análise. Simbolo: {}", simboloIntervalo);
            return;
        }
        if (estrategias == null || estrategias.isEmpty()) {
            log.warn("Nenhuma estratégia definida para o símbolo: {}", simboloIntervalo);
            return;
        }

        BigDecimal precoAtual = historicoPreco.get(historicoPreco.size() - 1).getPrice();
        BigDecimal precoAnterior = historicoPreco.get(historicoPreco.size() - 2).getPrice();

        for (Estrategia estrategia : estrategias) {
            BigDecimal precoAlvo = estrategia.getCondicoes().get(0).getValor();
            boolean isPrecoSubiu = ((precoAtual.compareTo(precoAlvo) > 0) && (precoAnterior.compareTo(precoAlvo) < 0));
            boolean isPrecoDesceu = ((precoAtual.compareTo(precoAlvo) < 0) && (precoAnterior.compareTo(precoAlvo) > 0));

            if (isPrecoSubiu || isPrecoDesceu) {
                sendMessage(simboloIntervalo, precoAtual);
                log.info("Alarme acionado para o símbolo: {} com preço alvo: {}", simboloIntervalo, precoAlvo);
            }
        }
        log.info("Analisando {} com estratégia de Preco", simboloIntervalo);
    }

    @Override
    public String getNome() {
        return TipoIndicadorEnum.PRECO.name();
    }

    private void sendMessage(String simboloIntervalo, BigDecimal precoAtual) {
        try {
            String simbolo = simboloIntervalo.split("-")[0];
            String[] parametros = new String[]{simbolo, String.valueOf(precoAtual)};

            kafkaService.sendMessageEstrategias(TipoIndicadorEnum.PRECO, parametros);
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem para o Kafka: {}", e.getMessage());
        }
    }

}
