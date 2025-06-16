package com.haefliger.cryptomonitor.ws;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Author diego-haefliger
 * Date 15/06/25
 */

@Service
@AllArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    @Override
    public void conect(Map<String, List<String>> symbolIntervals) {
        try {
            MultiSymbolPriceHandler handler = new MultiSymbolPriceHandler();
            WebSocketConnectionManager wsManager = new WebSocketConnectionManager(symbolIntervals, handler);

            wsManager.connect();
        } catch (Exception e) {
            log.error("Error connecting to WebSocket", e);
        }
    }

}
