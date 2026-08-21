package com.haefliger.cryptomonitor.dto.cache;

import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

public record CondicaoEstrategiaCacheDTO(
        Long id,
        String tipoIndicador,
        OperadorComparacaoEnum operador,
        String valor,
        LocalDateTime dateCreated) implements Serializable {
}
