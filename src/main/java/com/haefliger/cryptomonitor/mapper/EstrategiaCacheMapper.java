package com.haefliger.cryptomonitor.mapper;

import com.haefliger.cryptomonitor.dto.cache.CondicaoEstrategiaCacheDTO;
import com.haefliger.cryptomonitor.dto.cache.EstrategiaCacheDTO;
import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface EstrategiaCacheMapper {

    EstrategiaCacheDTO toCacheDTO(Estrategia estrategia);

    List<EstrategiaCacheDTO> toCacheDTOList(List<Estrategia> estrategias);

    CondicaoEstrategiaCacheDTO toCacheDTO(CondicaoEstrategia condicao);

    List<CondicaoEstrategiaCacheDTO> toCacheDTO(List<CondicaoEstrategia> condicoes);

    List<Estrategia> cacheToEntity(List<EstrategiaCacheDTO> estrategias);

    @Mapping(target = "id", ignore = true)
    Estrategia cacheToEntity(EstrategiaCacheDTO estrategia);

    @Mapping(target = "estrategia", ignore = true)
    CondicaoEstrategia cacheToEntity(CondicaoEstrategiaCacheDTO condicao);
}
