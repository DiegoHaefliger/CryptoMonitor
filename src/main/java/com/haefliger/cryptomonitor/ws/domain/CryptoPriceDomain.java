package com.haefliger.cryptomonitor.ws.domain;

import java.time.Instant;

public record CryptoPriceDomain(String symbol, String interval, double price, Instant timestamp) {
}
