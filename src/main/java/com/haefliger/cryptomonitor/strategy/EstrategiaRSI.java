package com.haefliger.cryptomonitor.strategy;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.service.KafkaService;
import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Author diego-haefliger
 * Date 25/06/25
 */

@Component
@Slf4j
public class EstrategiaRSI implements AnaliseEstrategia {

    private static final Integer PERIODO_RSI = 14;

    @Autowired
    private KafkaService kafkaService;

    @Override
    public void analisar(List<PrecoSimbolo> historicoPreco, String simboloIntervalo, List<Estrategia> estrategias) {
        List<Double> rsiValues = calcularRSI(historicoPreco);
        double rsiAnterior = rsiValues.get(0);
        double rsiAtual = rsiValues.get(1);
        for (Estrategia estrategia : estrategias) {
            if (estrategia.getCondicoes() == null || estrategia.getCondicoes().isEmpty()) {
                log.warn("Nenhuma condição definida para a estratégia de RSI no símbolo: {}", simboloIntervalo);
                continue;
            }

            double rsiAlvo = estrategia.getCondicoes().get(0).getValor().doubleValue();
            OperadorComparacaoEnum operacao = estrategia.getCondicoes().get(0).getOperador();
            boolean isValorAnterior = operacao.comparar(rsiAlvo, rsiAnterior);
            boolean isValorAlvo = operacao.comparar(rsiAlvo, rsiAtual);

            if (isValorAnterior && !isValorAlvo) {
                log.info("RSI atual {} anterior {} é {} que o alvo {} para o símbolo: {}", rsiAtual, rsiAnterior, operacao.getSimbolo(), rsiAlvo, simboloIntervalo);
                sendMessage(simboloIntervalo, rsiAlvo);
            }

        }
        log.info("Analisando {} com estratégia de RSI {}", simboloIntervalo, rsiAtual);
    }

    public List<Double> calcularRSI(List<PrecoSimbolo> historicoPreco) {
        final int periodo = PERIODO_RSI;

        if (historicoPreco == null || historicoPreco.size() < periodo + 1) {
            log.warn("Histórico de preço insuficiente para calcular RSI. Necessário: {}. Disponível: {}", periodo + 1, historicoPreco != null ? historicoPreco.size() : 0);
            return List.of(0.0, 0.0);
        }

        List<PrecoSimbolo> sortedHistorico = new ArrayList<>(historicoPreco);
        sortedHistorico.sort(Comparator.comparing(PrecoSimbolo::getTimestamp));

        double gain = 0.0;
        double loss = 0.0;

        for (int i = 1; i <= periodo; i++) {
            double change = sortedHistorico.get(i).getPrice().doubleValue() - sortedHistorico.get(i - 1).getPrice().doubleValue();
            if (change > 0) {
                gain += change;
            } else {
                loss += -change;
            }
        }

        double averageGain = gain / periodo;
        double averageLoss = loss / periodo;

        List<Double> rsiValues = new ArrayList<>();

        // Calcular o primeiro RSI
        double rs = averageLoss == 0 ? 0 : averageGain / averageLoss;
        double rsi = averageLoss == 0 ? 100.0 : 100 - (100 / (1 + rs));
        rsiValues.add(rsi);

        // Suavização de Wilder e cálculo dos próximos RSIs
        for (int i = periodo + 1; i < sortedHistorico.size(); i++) {
            double change = sortedHistorico.get(i).getPrice().doubleValue() - sortedHistorico.get(i - 1).getPrice().doubleValue();
            double currentGain = change > 0 ? change : 0;
            double currentLoss = change < 0 ? -change : 0;

            averageGain = ((averageGain * (periodo - 1)) + currentGain) / periodo;
            averageLoss = ((averageLoss * (periodo - 1)) + currentLoss) / periodo;

            rs = averageLoss == 0 ? 0 : averageGain / averageLoss;
            rsi = averageLoss == 0 ? 100.0 : 100 - (100 / (1 + rs));
            rsiValues.add(rsi);
        }

        // Retornar os dois últimos valores calculados
        int size = rsiValues.size();
        if (size >= 2) {
            return rsiValues.subList(size - 2, size);
        } else if (size == 1) {
            return List.of(rsiValues.get(0), rsiValues.get(0));
        } else {
            return List.of(0.0, 0.0);
        }
    }

    @Override
    public String getNome() {
        return TipoIndicadorEnum.RSI.name();
    }

    private void sendMessage(String simboloIntervalo, double rsi) {
        try {
            String simbolo = simboloIntervalo.split("-")[0];
            String intervalo = simboloIntervalo.split("-")[1];
            String[] parametros = new String[]{simbolo, String.valueOf(rsi), intervalo};

            kafkaService.sendMessageEstrategias(TipoIndicadorEnum.RSI, parametros);
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem para o Kafka: {}", e.getMessage());
        }
    }

}
