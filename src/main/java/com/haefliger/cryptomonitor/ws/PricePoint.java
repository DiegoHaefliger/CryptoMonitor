package com.haefliger.cryptomonitor.ws;


import lombok.Data;

import java.time.Instant;

/**
 * Author diego-haefliger
 * Date 6/14/25
 */

@Data
public class PricePoint {
    private final double price;
    private final Instant timestamp;
}
