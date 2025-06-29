package com.haefliger.cryptomonitor.strategy.dto;


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
public class PrecoSimbolo {
    private final BigDecimal price;
    private final Instant timestamp;
}
