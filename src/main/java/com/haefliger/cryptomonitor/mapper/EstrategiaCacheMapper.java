package com.haefliger.cryptomonitor.mapper;

import com.haefliger.cryptomonitor.dto.cache.CondicaoEstrategiaCacheDTO;
import com.haefliger.cryptomonitor.dto.cache.EstrategiaCacheDTO;
import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface EstrategiaCacheMapper {
    EstrategiaCacheMapper INSTANCE = Mappers.getMapper(EstrategiaCacheMapper.class);

    @Mapping(target = "condicoes", source = "condicoes")
    EstrategiaCacheDTO toCacheDTO(Estrategia estrategia);

    List<EstrategiaCacheDTO> toCacheDTOList(List<Estrategia> estrategias);

    CondicaoEstrategiaCacheDTO toCacheDTO(CondicaoEstrategia condicao);

    List<CondicaoEstrategiaCacheDTO> toCacheDTO(List<CondicaoEstrategia> condicoes);

    List<Estrategia> cacheToEntity(List<EstrategiaCacheDTO> estrategias);
}