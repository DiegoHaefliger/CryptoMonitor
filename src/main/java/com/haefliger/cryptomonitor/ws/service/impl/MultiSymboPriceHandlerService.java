package com.haefliger.cryptomonitor.ws.service.impl;

import static com.haefliger.cryptomonitor.utils.Constants.LIMIT_RECORDS;

import com.haefliger.cryptomonitor.mapper.PrecoSimboloMapper;
import com.haefliger.cryptomonitor.service.orchestrator.SimboloMonitoradoFactoryService;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import com.haefliger.cryptomonitor.ws.domain.PricePointDomain;
import com.haefliger.cryptomonitor.ws.service.MultiSymboPriceHandler;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author diego-haefliger
 * Date 14/06/25
 */
@ApplicationScoped
public class MultiSymboPriceHandlerService implements MultiSymboPriceHandler {

    private static final Logger log = LoggerFactory.getLogger(MultiSymboPriceHandlerService.class);

    private final SimboloMonitoradoFactoryService simboloMonitoradoFactoryService;
    private final PrecoSimboloMapper mapper;

    // estado interno do handler: nunca foi injetavel, o container nao tem bean de Map
    private final Map<String, Map<String, List<PricePointDomain>>> priceMap = new HashMap<>();

    MultiSymboPriceHandlerService(
            SimboloMonitoradoFactoryService simboloMonitoradoFactoryService,
            PrecoSimboloMapper mapper) {
        this.simboloMonitoradoFactoryService = simboloMonitoradoFactoryService;
        this.mapper = mapper;
    }

    @Override
    public synchronized void addPrice(
            String symbol, String interval, BigDecimal price, Instant timestamp) {
        List<PricePointDomain> prices =
                priceMap.computeIfAbsent(symbol, k -> new HashMap<>())
                        .computeIfAbsent(interval, k -> new ArrayList<>());

        prices.add(new PricePointDomain(price, timestamp));
        prices.sort(Comparator.comparing(PricePointDomain::timestamp).reversed());

        if (prices.size() > LIMIT_RECORDS) {
            prices.remove(prices.size() - 1);
            sendToOrchestrator(symbol, interval, prices);
        }

        if (prices.size() == LIMIT_RECORDS) {
            log.info(
                    "Preço adicionado para {} [{}]: {} em {}",
                    symbol,
                    interval,
                    prices.get(0).price(),
                    prices.get(0).timestamp());
        }
    }

    @Override
    public synchronized void addPricesHistorical(
            String symbol, String interval, List<PricePointDomain> prices) {
        List<PricePointDomain> existingPrices =
                priceMap.computeIfAbsent(symbol, k -> new HashMap<>())
                        .computeIfAbsent(interval, k -> new ArrayList<>());
        existingPrices.addAll(prices);
        existingPrices.sort(Comparator.comparing(PricePointDomain::timestamp).reversed());

        if (existingPrices.size() >= LIMIT_RECORDS) {
            log.info(
                    "Preços adicionados para {} [{}]: {} em {}",
                    symbol,
                    interval,
                    existingPrices.get(0).price(),
                    existingPrices.get(0).timestamp());
        }
    }

    public List<PricePointDomain> getPrices(String symbol, String interval) {
        return Collections.unmodifiableList(
                priceMap.getOrDefault(symbol, Collections.emptyMap())
                        .getOrDefault(interval, Collections.emptyList()));
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

        return prices.stream().map(mapper::wsToMonitor).toList();
    }

    private void sendToOrchestrator(String symbol, String interval, List<PricePointDomain> prices) {
        String simboloIntervalo = getKey(symbol, interval);
        simboloMonitoradoFactoryService.criarSimbolosMonitorados(
                convertToPrecoSimbolos(prices), simboloIntervalo);
    }
}
