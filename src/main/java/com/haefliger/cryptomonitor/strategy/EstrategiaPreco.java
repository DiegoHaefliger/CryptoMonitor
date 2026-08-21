package com.haefliger.cryptomonitor.strategy;

import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.service.KafkaService;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class EstrategiaPreco implements AnaliseEstrategia {

    private static final Logger log = LoggerFactory.getLogger(EstrategiaPreco.class);

    private final KafkaService kafkaService;

    EstrategiaPreco(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Override
    public void analisar(List<PrecoSimboloDomain> historicoPreco, String simboloIntervalo, List<Estrategia> estrategias) {
        if (historicoPreco == null || historicoPreco.size() < 2) {
            log.warn("Histórico de preço insuficiente para análise. Simbolo: {}", simboloIntervalo);
            return;
        }
        if (estrategias == null || estrategias.isEmpty()) {
            log.warn("Nenhuma estratégia definida para o símbolo: {}", simboloIntervalo);
            return;
        }

        BigDecimal precoAtual = historicoPreco.get(historicoPreco.size() - 1).price();
        BigDecimal precoAnterior = historicoPreco.get(historicoPreco.size() - 2).price();

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
