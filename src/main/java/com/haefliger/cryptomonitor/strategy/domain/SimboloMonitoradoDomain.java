package com.haefliger.cryptomonitor.strategy.domain;


import com.haefliger.cryptomonitor.strategy.AnaliseEstrategia;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Author diego-haefliger
 * Date 25/06/25
 */
@Data
@Builder
public class SimboloMonitoradoDomain {
    private String simbolo;
    private List<AnaliseEstrategia> estrategias;
}
