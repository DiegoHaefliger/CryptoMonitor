package com.haefliger.cryptomonitor.strategy.domain;

import com.haefliger.cryptomonitor.strategy.AnaliseEstrategia;
import java.util.List;

public record SimboloMonitoradoDomain(String simbolo, List<AnaliseEstrategia> estrategias) {}
