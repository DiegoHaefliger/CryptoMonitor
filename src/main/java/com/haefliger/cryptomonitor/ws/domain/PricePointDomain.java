package com.haefliger.cryptomonitor.ws.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PricePointDomain(BigDecimal price, Instant timestamp) {}
