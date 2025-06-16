package com.haefliger.cryptomonitor.ws;


import java.time.Instant;
import java.util.List;

/**
 * Author diego-haefliger
 * Date 14/06/25
 */

public interface PriceHandler {

    void addPrice(String symbol, String interval, double price, Instant timestamp);

    void addPricesHistorical(String symbol, String interval, List<PricePoint> prices);

    List<PricePoint> getPrices(String symbol, String interval);
}
