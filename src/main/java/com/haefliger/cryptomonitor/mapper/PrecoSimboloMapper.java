package com.haefliger.cryptomonitor.mapper;


import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import com.haefliger.cryptomonitor.ws.PricePoint;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

/**
 * Author diego-haefliger
 * Date 26/06/25
 */

@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface PrecoSimboloMapper {

    PrecoSimboloDomain wsToMonitor(PricePoint pricePoints);

}
