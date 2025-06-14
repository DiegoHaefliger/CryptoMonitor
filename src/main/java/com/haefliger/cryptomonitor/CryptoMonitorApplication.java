package com.haefliger.cryptomonitor;

import com.haefliger.cryptomonitor.ws.MultiSymbolPriceHandler;
import com.haefliger.cryptomonitor.ws.WebSocketConnectionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class CryptoMonitorApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(CryptoMonitorApplication.class, args);

        Map<String, List<String>> symbolIntervals = new HashMap<>();
        symbolIntervals.put("BTCUSDT", List.of("1", "5", "15"));
        symbolIntervals.put("ETHUSDT", List.of("1", "5"));
        symbolIntervals.put("SOLUSDT", List.of("15"));

        MultiSymbolPriceHandler handler = new MultiSymbolPriceHandler();

        WebSocketConnectionManager wsManager = new WebSocketConnectionManager(symbolIntervals, handler);

        wsManager.connect();

    }

}
