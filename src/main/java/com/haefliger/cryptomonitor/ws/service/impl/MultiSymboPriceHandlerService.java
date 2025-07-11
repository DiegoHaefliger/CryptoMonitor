package com.haefliger.cryptomonitor.ws.service.impl;


import com.haefliger.cryptomonitor.mapper.PrecoSimboloMapper;
import com.haefliger.cryptomonitor.service.orchestrator.SimboloMonitoradoFactoryService;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import com.haefliger.cryptomonitor.ws.service.MultiSymboPriceHandler;
import com.haefliger.cryptomonitor.ws.domain.PricePointDomain;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static com.haefliger.cryptomonitor.utils.Constants.LIMIT_RECORDS;


/**
 * Author diego-haefliger
 * Date 14/06/25
 */

@Service
@AllArgsConstructor
@Slf4j
public class MultiSymboPriceHandlerService implements MultiSymboPriceHandler {

    private final SimboloMonitoradoFactoryService simboloMonitoradoFactoryService;
    private final Map<String, Map<String, List<PricePointDomain>>> priceMap;
    private final PrecoSimboloMapper mapper;

    @Override
    public synchronized void addPrice(String symbol, String interval, BigDecimal price, Instant timestamp) {
        List<PricePointDomain> prices = priceMap
                .computeIfAbsent(symbol, k -> new HashMap<>())
                .computeIfAbsent(interval, k -> new ArrayList<>());

        prices.add(new PricePointDomain(price, timestamp));
        prices.sort(Comparator.comparing(PricePointDomain::getTimestamp).reversed());

        if (prices.size() > LIMIT_RECORDS) {
            prices.remove(prices.size() - 1);
            sendToOrchestrator(symbol, interval, prices);
        }

        if (prices.size() == LIMIT_RECORDS) {
            log.info("Preço adicionado para {} [{}]: {} em {}", symbol, interval, prices.get(0).getPrice(), prices.get(0).getTimestamp());
        }
    }

    @Override
    public synchronized void addPricesHistorical(String symbol, String interval, List<PricePointDomain> prices) {
        List<PricePointDomain> existingPrices = priceMap
                .computeIfAbsent(symbol, k -> new HashMap<>())
                .computeIfAbsent(interval, k -> new ArrayList<>());
        existingPrices.addAll(prices);
        existingPrices.sort(Comparator.comparing(PricePointDomain::getTimestamp).reversed());

        if (existingPrices.size() >= LIMIT_RECORDS) {
            log.info("Preços adicionados para {} [{}]: {} em {}", symbol, interval, existingPrices.get(0).getPrice(), existingPrices.get(0).getTimestamp());
        }
    }

    public List<PricePointDomain> getPrices(String symbol, String interval) {
        return Collections.unmodifiableList(
                priceMap.getOrDefault(symbol, Collections.emptyMap())
                        .getOrDefault(interval, Collections.emptyList())
        );
    }

    public void clearAll() {
        priceMap.clear();
    }

    private String getKey(String symbol, String interval) {
        return String.format("%s-%s", symbol, interval);
    }

    private List<PrecoSimboloDomain> convertToPrecoSimbolos(List<PricePointDomain> prices) {
        if (prices == null || prices.isEmpty()) {
            return Collections.emptyList();
        }

        return prices.stream()
                .map(mapper::wsToMonitor)
                .toList();
    }

    private void sendToOrchestrator(String symbol, String interval, List<PricePointDomain> prices) {
        String simboloIntervalo = getKey(symbol, interval);
        simboloMonitoradoFactoryService.criarSimbolosMonitorados(convertToPrecoSimbolos(prices), simboloIntervalo);
    }
}
