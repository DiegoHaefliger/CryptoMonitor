package com.haefliger.cryptomonitor.ws.service;

import com.haefliger.cryptomonitor.ws.domain.PricePointDomain;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface MultiSymboPriceHandler {

    void addPrice(String symbol, String interval, BigDecimal price, Instant timestamp);

    void addPricesHistorical(String symbol, String interval, List<PricePointDomain> prices);
}
