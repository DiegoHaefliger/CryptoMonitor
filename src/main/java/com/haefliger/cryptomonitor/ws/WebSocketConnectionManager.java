package com.haefliger.cryptomonitor.ws;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.List;

@Slf4j
public class WebSocketConnectionManager {
    @Getter
    private BybitWebSocketClient client;
    private final PriceHandler handler;
    private final Map<String, List<String>> symbolIntervals;

    public WebSocketConnectionManager(Map<String, List<String>> symbolIntervals, PriceHandler handler) {
        this.symbolIntervals = symbolIntervals;
        this.handler = handler;
    }

    public synchronized void connect() throws Exception {
        if (client != null && client.isOpen()) {
            log.info("WebSocket already open. Disconnecting before reconnecting...");
            disconnect();
        }
        client = new BybitWebSocketClient(symbolIntervals, handler);
        client.connect();
        log.info("WebSocket connection initiated.");
    }

    public synchronized void disconnect() {
        if (client != null && client.isOpen()) {
            client.close();
            log.info("WebSocket connection closed.");
        } else {
            log.info("No open WebSocket connection to close.");
        }
        client = null;
    }

    public synchronized boolean isConnected() {
        return client != null && client.isOpen();
    }

}