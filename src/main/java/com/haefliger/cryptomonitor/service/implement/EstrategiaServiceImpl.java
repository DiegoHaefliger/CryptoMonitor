package com.haefliger.cryptomonitor.service.implement;

import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.EstrategiaResponse;
import com.haefliger.cryptomonitor.service.EstrategiaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EstrategiaServiceImpl implements EstrategiaService {

    @Override
    public EstrategiaResponse salvarEstrategia(EstrategiaRequest estrategiaRequest) {
        log.info("Salvando estratégia: {}", estrategiaRequest);
        return null;
    }

}
