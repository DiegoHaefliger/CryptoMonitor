package com.haefliger.cryptomonitor.mapper;

import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import com.haefliger.cryptomonitor.ws.domain.PricePointDomain;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface PrecoSimboloMapper {

    PrecoSimboloDomain wsToMonitor(PricePointDomain pricePointsDomain);
}
