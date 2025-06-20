package com.haefliger.cryptomonitor.ws;


import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.mapper.EstrategiaWebSocketMapper;
import com.haefliger.cryptomonitor.service.RedisService;
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

    private static final MultiSymbolPriceHandler handler = new MultiSymbolPriceHandler();
    private static WebSocketConnectionManager wsManager = null;
    private final EstrategiaWebSocketMapper estrategiaWebSocketMapper;
    private final RedisService redisService;

    @Override
    public synchronized void conect(Map<String, List<String>> symbolIntervals) {
        try {
            if (wsManager == null) {
                wsManager = new WebSocketConnectionManager(symbolIntervals, handler);
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

            Map<String, List<String>> symbolIntervals = estrategiaWebSocketMapper.toSymbolIntervals(estrategias);
            conect(symbolIntervals);
        } catch (RuntimeException e) {
            log.error("Erro ao Retorna estratégias para o WS: ", e);
            throw new RuntimeException("Erro ao Retorna estratégias para o WS", e);
        }
    }

}
