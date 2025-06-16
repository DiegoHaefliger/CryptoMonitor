package com.haefliger.cryptomonitor.mapper;

import com.haefliger.cryptomonitor.entity.Estrategia;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EstrategiaWebSocketMapper {

    EstrategiaWebSocketMapper INSTANCE = Mappers.getMapper(EstrategiaWebSocketMapper.class);

    default Map<String, List<String>> toSymbolIntervals(List<Estrategia> estrategias) {
        if (estrategias == null) return Collections.emptyMap();
        return estrategias.stream()
                .collect(Collectors.groupingBy(
                        Estrategia::getSimbolo,
                        Collectors.mapping(Estrategia::getIntervalo, Collectors.toSet())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Set<String> intervals = new HashSet<>(entry.getValue());
                            intervals.add("1");
                            List<String> intervalList = new ArrayList<>(intervals);
                            Collections.sort(intervalList);
                            return intervalList;
                        }
                ));
    }
}

