package com.haefliger.cryptomonitor.mapper;

import com.haefliger.cryptomonitor.dto.request.estrategia.CondicaoRequest;
import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaListaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.SalvarEstrategiaResponse;
import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EstrategiaMapper {

    Estrategia requestToEntityEstrategia(EstrategiaRequest estrategiaRequest);

    List<CondicaoEstrategia> requestToEntityCondicaoEstrategia(List<CondicaoRequest> condicaoRequest);

    SalvarEstrategiaResponse longToSalvarEstrategiaResponse(Long id);

    CondicaoEstrategia condicaoRequestToCondicaoEstrategia(CondicaoRequest condicaoRequest);

    @IterableMapping(qualifiedByName = "entityToBuscarEstrategiaListaResponse")
    List<BuscarEstrategiaListaResponse> entityListToBuscarEstrategiaListaResponse(List<Estrategia> estrategias);

    @Named("entityToBuscarEstrategiaListaResponse")
    BuscarEstrategiaListaResponse entityToBuscarEstrategiaListaResponse(Estrategia estrategia);

    default BuscarEstrategiaResponse entityListToBuscarEstrategiaResponse(List<Estrategia> estrategias) {
        List<BuscarEstrategiaListaResponse> lista = entityListToBuscarEstrategiaListaResponse(estrategias);
        return BuscarEstrategiaResponse.builder()
                .estrategias(lista)
                .build();
    }
}
