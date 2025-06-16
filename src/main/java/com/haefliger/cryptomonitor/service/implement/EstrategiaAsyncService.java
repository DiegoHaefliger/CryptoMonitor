package com.haefliger.cryptomonitor.service.implement;

import com.haefliger.cryptomonitor.ws.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstrategiaAsyncService {

    private final WebSocketService webSocketService;

    @Async
    public void atualizaEstrategiasWS() {
        try {
            log.info("Atualizando estratégias via WebSocket de forma assíncrona");
            webSocketService.atualizaEstrategiasWS();
        } catch (Exception e) {
            log.error("Erro ao atualizar estratégias via WebSocket: {}", e.getMessage(), e);
        }
    }
}

