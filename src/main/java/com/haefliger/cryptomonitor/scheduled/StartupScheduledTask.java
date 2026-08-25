package com.haefliger.cryptomonitor.scheduled;

import com.haefliger.cryptomonitor.service.implement.EstrategiaAsyncService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class StartupScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(StartupScheduledTask.class);

    private final EstrategiaAsyncService estrategiaAsyncService;

    StartupScheduledTask(EstrategiaAsyncService estrategiaAsyncService) {
        this.estrategiaAsyncService = estrategiaAsyncService;
    }

    void onStart(@Observes StartupEvent event) {
        try {
            log.info("Inicia agendamento de estratégia do Web Socket");
            estrategiaAsyncService.atualizaEstrategiasWS();
        } catch (Exception e) {
            log.error("Falha ao iniciar agendamento de estratégia do Web Socket");
            throw new ServiceException(
                    "Falha ao iniciar agendamento de estratégia do Web Socket", e);
        }
    }
}
