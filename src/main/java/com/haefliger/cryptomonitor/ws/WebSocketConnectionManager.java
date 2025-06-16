package com.haefliger.cryptomonitor.ws;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.List;

@Slf4j
public class WebSocketConnectionManager {
    @Getter
    private WebSocketClient client;
    private final PriceHandler handler;
    private Map<String, List<String>> symbolIntervals;

    public WebSocketConnectionManager(Map<String, List<String>> symbolIntervals, PriceHandler handler) {
        this.symbolIntervals = symbolIntervals;
        this.handler = handler;
    }

    public synchronized void connect() throws Exception {
        if (client != null && client.isOpen()) {
            log.info("WebSocket já está aberto. Ignorando reconexão.");
            return;
        }
        client = new WebSocketClient(symbolIntervals, handler);
        client.connect();
        log.info("Conexão WebSocket iniciada.");
    }

    public synchronized void disconnect() {
        if (client != null && client.isOpen()) {
            client.close();
            log.info("Conexão WebSocket fechada.");
        } else {
            log.info("Nenhuma conexão WebSocket aberta para fechar.");
        }
        client = null;
    }

    public synchronized boolean isConnected() {
        return client != null && client.isOpen();
    }

    public synchronized void updateSubscriptions(Map<String, List<String>> newSymbolIntervals) throws Exception {
        if (client != null && client.isOpen()) {
            client.updateSubscriptions(newSymbolIntervals);
            this.symbolIntervals = newSymbolIntervals;
            log.info("Assinaturas do WebSocket atualizadas.");
        } else {
            // Se não estiver conectado, atualiza symbolIntervals e conecta
            this.symbolIntervals = newSymbolIntervals;
            connect();
        }
    }

}