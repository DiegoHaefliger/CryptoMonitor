package com.haefliger.cryptomonitor.scheduled;

import com.haefliger.cryptomonitor.service.implement.EstrategiaAsyncService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class StartupScheduledTask {

    private final EstrategiaAsyncService estrategiaAsyncService;

    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void scheduleTaskAfterStartup() {
        try {
            log.info("Inicia agendamento de estratégia do Web Socket");
            estrategiaAsyncService.atualizaEstrategiasWS();
        } catch (Exception e) {
            log.error("Falha ao iniciar agendamento de estratégia do Web Socket");
            throw new ServiceException("Falha ao iniciar agendamento de estratégia do Web Socket", e);
        }
    }

}