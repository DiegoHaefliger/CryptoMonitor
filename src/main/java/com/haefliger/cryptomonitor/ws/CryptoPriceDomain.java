package com.haefliger.cryptomonitor.ws;


import lombok.Data;

import java.time.Instant;

/**
 * Author diego-haefliger
 * Date 14/06/25
 */

@Data
public class CryptoPriceDomain {
    private final String symbol;
    private final String interval;
    private final double price;
    private final Instant timestamp;
}
