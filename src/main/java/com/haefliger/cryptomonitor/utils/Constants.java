package com.haefliger.cryptomonitor.utils;


import org.springframework.context.annotation.Configuration;

/**
 * Author diego-haefliger
 * Date 14/06/25
 */

@Configuration
public class Constants {
    public static final String WEBSOCKET_URL_LINEAR = "wss://stream.bybit.com/v5/public/linear";
    public static final String WEBSOCKET_URL_KLINE = "https://api.bybit.com/v5/market/kline";
    public static final String TOPIC = "topic";
    public static final Integer LIMIT_RECORDS = 100;
    public static final String KLINE_PREFIX = "kline.";
    public static final String OP = "op";
    public static final String SUBSCRIBE = "subscribe";
    public static final String ARGS = "args";
}
