package com.haefliger.cryptomonitor.ws;


import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;

import static com.haefliger.cryptomonitor.utils.Constants.LIMIT_RECORDS;


/**
 * Author diego-haefliger
 * Date 14/06/25
 */

@Slf4j
public class MultiSymbolPriceHandler implements PriceHandler {

    private final Map<String, Map<String, List<PricePoint>>> priceMap = new HashMap<>();

    @Override
    public synchronized void addPrice(String symbol, String interval, double price, Instant timestamp) {
        List<PricePoint> prices = priceMap
                .computeIfAbsent(symbol, k -> new HashMap<>())
                .computeIfAbsent(interval, k -> new ArrayList<>());

        prices.add(new PricePoint(price, timestamp));
        prices.sort(Comparator.comparing(PricePoint::getTimestamp).reversed());

        if (prices.size() > LIMIT_RECORDS) {
            prices.remove(prices.size() - 1);
        }

        if (prices.size() == LIMIT_RECORDS) {
            log.info("Preço adicionado para {} [{}]: {} em {}", symbol, interval, prices.get(0).getPrice(), prices.get(0).getTimestamp());
        }
    }

    @Override
    public synchronized void addPricesHistorical(String symbol, String interval, List<PricePoint> prices) {
        List<PricePoint> existingPrices = priceMap
                .computeIfAbsent(symbol, k -> new HashMap<>())
                .computeIfAbsent(interval, k -> new ArrayList<>());
        existingPrices.addAll(prices);
        existingPrices.sort(Comparator.comparing(PricePoint::getTimestamp).reversed());

        if (existingPrices.size() >= LIMIT_RECORDS) {
            log.info("Preços adicionados para {} [{}]: {} em {}", symbol, interval, existingPrices.get(0).getPrice(), existingPrices.get(0).getTimestamp());
        }
    }

    public List<PricePoint> getPrices(String symbol, String interval) {
        return Collections.unmodifiableList(
                priceMap.getOrDefault(symbol, Collections.emptyMap())
                        .getOrDefault(interval, Collections.emptyList())
        );
    }

    public void clearAll() {
        priceMap.clear();
    }
}
