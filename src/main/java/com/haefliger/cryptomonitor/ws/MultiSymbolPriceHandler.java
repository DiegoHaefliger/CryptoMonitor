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
    public synchronized void addPrice(String symbol, String interval, double price, Instant timestamp, boolean removeOldest) {
        List<PricePoint> prices = priceMap
                .computeIfAbsent(symbol, k -> new HashMap<>())
                .computeIfAbsent(interval, k -> new ArrayList<>());

        prices.add(new PricePoint(price, timestamp));

        if (removeOldest && prices.size() > LIMIT_RECORDS) {
            prices.remove(prices.size() - 1);
        }

        if (prices.size() == LIMIT_RECORDS) {
            prices.sort(Comparator.comparing(PricePoint::getTimestamp).reversed());
            log.info("Added price for {} [{}]: {} at {}", symbol, interval, prices.get(0).getPrice(), prices.get(0).getTimestamp());
        }
    }

    public List<PricePoint> getPrices(String symbol, String interval) {
        return Collections.unmodifiableList(
                priceMap.getOrDefault(symbol, Collections.emptyMap())
                        .getOrDefault(interval, Collections.emptyList())
        );
    }

    public Set<String> getTrackedSymbols() {
        return priceMap.keySet();
    }

    public Set<String> getTrackedIntervals(String symbol) {
        return priceMap.getOrDefault(symbol, Collections.emptyMap()).keySet();
    }
}
