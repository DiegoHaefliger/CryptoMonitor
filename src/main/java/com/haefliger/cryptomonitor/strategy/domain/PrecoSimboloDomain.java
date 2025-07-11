package com.haefliger.cryptomonitor.strategy.domain;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Author diego-haefliger
 * Date 25/06/25
 */

@Data
@Builder
public class PrecoSimboloDomain {
    private final BigDecimal price;
    private final Instant timestamp;
}
