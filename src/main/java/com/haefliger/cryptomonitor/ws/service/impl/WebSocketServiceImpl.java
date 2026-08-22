package com.haefliger.cryptomonitor.ws.service.impl;

import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.mapper.EstrategiaWebSocketMapper;
import com.haefliger.cryptomonitor.service.RedisService;
import com.haefliger.cryptomonitor.ws.WebSocketConnectionManager;
import com.haefliger.cryptomonitor.ws.service.WebSocketService;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class WebSocketServiceImpl implements WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServiceImpl.class);

    private final MultiSymboPriceHandlerService handler;
    private final EstrategiaWebSocketMapper estrategiaWebSocketMapper;
    private final RedisService redisService;
    private final int maxReconnectAttempts;
    private final int baseReconnectDelaySeconds;

    private WebSocketConnectionManager wsManager;

    WebSocketServiceImpl(
            MultiSymboPriceHandlerService handler,
            EstrategiaWebSocketMapper estrategiaWebSocketMapper,
            RedisService redisService,
            @ConfigProperty(name = "websocket.max-reconnect-attempts", defaultValue = "10")
                    int maxReconnectAttempts,
            @ConfigProperty(name = "websocket.base-reconnect-delay-seconds", defaultValue = "5")
                    int baseReconnectDelaySeconds) {
        this.handler = handler;
        this.estrategiaWebSocketMapper = estrategiaWebSocketMapper;
        this.redisService = redisService;
        this.maxReconnectAttempts = maxReconnectAttempts;
        this.baseReconnectDelaySeconds = baseReconnectDelaySeconds;
    }

    @Override
    public synchronized void conect(Map<String, List<String>> symbolIntervals) {
        try {
            if (wsManager == null) {
                wsManager =
                        new WebSocketConnectionManager(
                                symbolIntervals,
                                handler,
                                maxReconnectAttempts,
                                baseReconnectDelaySeconds);
                wsManager.connect();
            } else {
                wsManager.updateSubscriptions(symbolIntervals);
            }
        } catch (Exception e) {
            log.error("Error connecting/updating WebSocket", e);
        }
    }

    @Override
    public synchronized void disconnect() {
        if (wsManager != null) {
            wsManager.disconnect();
            wsManager = null;
        }
    }

    @Override
    public void atualizaEstrategiasWS() {
        try {
            log.info("Retorna estratégias para o WS");
            List<Estrategia> estrategias = redisService.buscarEstrategiasAtivasRedis();

            Map<String, List<String>> symbolIntervals =
                    estrategiaWebSocketMapper.toSymbolIntervals(estrategias);
            conect(symbolIntervals);
        } catch (RuntimeException e) {
            log.error("Erro ao Retorna estratégias para o WS: ", e);
            throw new RuntimeException("Erro ao Retorna estratégias para o WS", e);
        }
    }
}
