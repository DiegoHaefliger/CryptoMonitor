package com.haefliger.cryptomonitor.service;

import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.EstrategiaResponse;

public interface EstrategiaService {

    EstrategiaResponse salvarEstrategia(EstrategiaRequest estrategiaRequest);

}
