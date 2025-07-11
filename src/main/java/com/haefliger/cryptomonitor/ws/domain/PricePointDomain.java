package com.haefliger.cryptomonitor.ws.domain;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PricePointDomain {
    private final BigDecimal price;
    private final Instant timestamp;
}
