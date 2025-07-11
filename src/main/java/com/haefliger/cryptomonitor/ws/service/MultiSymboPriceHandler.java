package com.haefliger.cryptomonitor.ws.service;


import com.haefliger.cryptomonitor.ws.domain.PricePointDomain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Author diego-haefliger
 * Date 14/06/25
 */

public interface MultiSymboPriceHandler {

    void addPrice(String symbol, String interval, BigDecimal price, Instant timestamp);

    void addPricesHistorical(String symbol, String interval, List<PricePointDomain> prices);

    List<PricePointDomain> getPrices(String symbol, String interval);
}
