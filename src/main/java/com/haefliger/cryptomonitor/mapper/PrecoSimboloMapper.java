package com.haefliger.cryptomonitor.mapper;


import com.haefliger.cryptomonitor.strategy.dto.PrecoSimbolo;
import com.haefliger.cryptomonitor.ws.PricePoint;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

/**
 * Author diego-haefliger
 * Date 26/06/25
 */

@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface PrecoSimboloMapper {

    PrecoSimbolo wsToMonitor(PricePoint pricePoints);

}
