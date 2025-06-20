package com.haefliger.cryptomonitor.utils;


import org.springframework.context.annotation.Configuration;

/**
 * Author diego-haefliger
 * Date 14/06/25
 */

@Configuration
public class Constants {
    public static final String WEBSOCKET_URL_LINEAR = "wss://stream.bybit.com/v5/public/linear";
    public static final String WEBSOCKET_URL_KLINE = "https://api.bybit.com/v5/market/kline?category=linear&symbol=%s&interval=%s&limit=%s";
    public static final Integer LIMIT_RECORDS = 100;
}
