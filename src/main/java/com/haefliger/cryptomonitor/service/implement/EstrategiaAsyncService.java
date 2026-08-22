package com.haefliger.cryptomonitor.service.implement;

import com.haefliger.cryptomonitor.ws.service.WebSocketService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EstrategiaAsyncService {

    private static final Logger log = LoggerFactory.getLogger(EstrategiaAsyncService.class);

    private final WebSocketService webSocketService;
    private final ManagedExecutor executor;

    EstrategiaAsyncService(WebSocketService webSocketService, ManagedExecutor executor) {
        this.webSocketService = webSocketService;
        this.executor = executor;
    }

    public void atualizaEstrategiasWS() {
        executor.execute(
                () -> {
                    try {
                        log.info("Atualizando estratégias via WebSocket de forma assíncrona");
                        webSocketService.atualizaEstrategiasWS();
                    } catch (Exception e) {
                        log.error(
                                "Erro ao atualizar estratégias via WebSocket: {}",
                                e.getMessage(),
                                e);
                    }
                });
    }
}
