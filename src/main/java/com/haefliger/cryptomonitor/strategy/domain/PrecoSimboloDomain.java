package com.haefliger.cryptomonitor.strategy.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PrecoSimboloDomain(BigDecimal price, Instant timestamp) {}
