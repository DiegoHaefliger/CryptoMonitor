package com.haefliger.cryptomonitor.service.bybit.market;


/**
 * Author diego-haefliger
 * Date 14/06/25
 */
public interface PriceListener {
    void onTick(PriceTick tick);
}
