package com.haefliger.cryptomonitor.ws;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Author diego-haefliger
 * Date 6/14/25
 */

@Data
@Builder
public class PricePoint {
    private final BigDecimal price;
    private final Instant timestamp;
}
