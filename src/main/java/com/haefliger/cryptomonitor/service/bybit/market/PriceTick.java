package com.haefliger.cryptomonitor.service.bybit.market;


import lombok.Data;

/**
 * Author diego-haefliger
 * Date 14/06/25
 */

@Data
public class PriceTick {
    private final String symbol;
    private final double price;
    private final long timestamp;
}
