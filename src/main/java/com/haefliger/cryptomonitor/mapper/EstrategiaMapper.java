package com.haefliger.cryptomonitor.mapper;

import com.haefliger.cryptomonitor.dto.request.estrategia.CondicaoRequest;
import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaListaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.SalvarEstrategiaResponse;
import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface EstrategiaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateLastUpdate", ignore = true)
    Estrategia requestToEntityEstrategia(EstrategiaRequest estrategiaRequest, Boolean ativo);

    List<CondicaoEstrategia> requestToEntityCondicaoEstrategia(
            List<CondicaoRequest> condicaoRequest);

    SalvarEstrategiaResponse longToSalvarEstrategiaResponse(Long id);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estrategia", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    CondicaoEstrategia condicaoRequestToCondicaoEstrategia(CondicaoRequest condicaoRequest);

    @IterableMapping(qualifiedByName = "entityToBuscarEstrategiaListaResponse")
    List<BuscarEstrategiaListaResponse> entityListToBuscarEstrategiaListaResponse(
            List<Estrategia> estrategias);

    @Named("entityToBuscarEstrategiaListaResponse")
    BuscarEstrategiaListaResponse entityToBuscarEstrategiaListaResponse(Estrategia estrategia);

    default BuscarEstrategiaResponse entityListToBuscarEstrategiaResponse(
            List<Estrategia> estrategias) {
        return new BuscarEstrategiaResponse(entityListToBuscarEstrategiaListaResponse(estrategias));
    }
}
