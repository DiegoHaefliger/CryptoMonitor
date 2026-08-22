package com.haefliger.cryptomonitor.ws;

import com.haefliger.cryptomonitor.ws.service.MultiSymboPriceHandler;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConnectionManager.class);

    private WebSocketClient client;
    private final MultiSymboPriceHandler handler;
    private Map<String, List<String>> symbolIntervals;
    private final ScheduledExecutorService reconnectExecutor =
            Executors.newSingleThreadScheduledExecutor();
    private int reconnectAttempts = 0;

    private final int maxReconnectAttempts;
    private final int baseReconnectDelaySeconds;

    public WebSocketConnectionManager(
            Map<String, List<String>> symbolIntervals,
            MultiSymboPriceHandler handler,
            int maxReconnectAttempts,
            int baseReconnectDelaySeconds) {
        this.symbolIntervals = symbolIntervals;
        this.handler = handler;
        this.maxReconnectAttempts = maxReconnectAttempts;
        this.baseReconnectDelaySeconds = baseReconnectDelaySeconds;
    }

    public WebSocketClient getClient() {
        return client;
    }

    public synchronized void connect() throws Exception {
        if (client != null && client.isOpen()) {
            log.info("WebSocket já está aberto. Ignorando reconexão.");
            return;
        }
        client = new WebSocketClient(symbolIntervals, handler);
        client.setConnectionListener(this::onConnectionLost);
        client.connect();
        log.info("Conexão WebSocket iniciada.");
        reconnectAttempts = 0;
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

    public synchronized void updateSubscriptions(Map<String, List<String>> newSymbolIntervals)
            throws Exception {
        if (client != null && client.isOpen()) {
            client.updateSubscriptions(newSymbolIntervals);
            this.symbolIntervals = newSymbolIntervals;
            log.info("Assinaturas do WebSocket atualizadas.");
        } else {
            this.symbolIntervals = newSymbolIntervals;
            connect();
        }
    }

    private void onConnectionLost() {
        Runnable retryTask =
                new Runnable() {
                    @Override
                    public void run() {
                        if (reconnectAttempts >= maxReconnectAttempts) {
                            log.error(
                                    "Limite máximo de tentativas de reconexão do WebSocket atingido.");
                            return;
                        }
                        int delay = baseReconnectDelaySeconds * (reconnectAttempts + 1);
                        log.warn(
                                "WebSocket desconectado. Tentando reconectar em {} segundos (tentativa {}/{})...",
                                delay,
                                reconnectAttempts + 1,
                                maxReconnectAttempts);
                        try {
                            connect();
                            log.info("Reconexão WebSocket realizada com sucesso.");
                        } catch (Exception e) {
                            log.error("Falha ao tentar reconectar WebSocket", e);
                            reconnectAttempts++;
                            reconnectExecutor.schedule(this, delay, TimeUnit.SECONDS);
                        }
                    }
                };
        reconnectExecutor.schedule(retryTask, baseReconnectDelaySeconds, TimeUnit.SECONDS);
    }
}
