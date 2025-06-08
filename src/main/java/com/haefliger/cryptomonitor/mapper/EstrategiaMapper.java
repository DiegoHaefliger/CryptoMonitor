package com.haefliger.cryptomonitor.mapper;

import com.haefliger.cryptomonitor.dto.request.estrategia.CondicaoRequest;
import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.EstrategiaResponse;
import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EstrategiaMapper {

    Estrategia requestToEntityEstrategia(EstrategiaRequest estrategiaRequest);

    List<CondicaoEstrategia> requestToEntityCondicaoEstrategia(List<CondicaoRequest> condicaoRequest);

    EstrategiaResponse longToResponseEstrategia(Long id);

    CondicaoEstrategia condicaoRequestToCondicaoEstrategia(CondicaoRequest condicaoRequest);
}
