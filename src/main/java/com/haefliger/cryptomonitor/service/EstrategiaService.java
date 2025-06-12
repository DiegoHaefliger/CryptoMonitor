package com.haefliger.cryptomonitor.service;

import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.SalvarEstrategiaResponse;

public interface EstrategiaService {

    SalvarEstrategiaResponse salvarEstrategia(EstrategiaRequest estrategiaRequest);

    BuscarEstrategiaResponse buscarEstrategia(Boolean ativo);

    void deletarEstrategia(Long id);

    void statusEstrategia(Long id, Boolean ativo, Boolean permanente);
}
