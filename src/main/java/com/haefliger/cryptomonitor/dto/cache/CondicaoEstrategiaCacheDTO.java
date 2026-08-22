package com.haefliger.cryptomonitor.dto.cache;

import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.time.LocalDateTime;

// Serializado direto pelo ObjectMapper (cache Redis), fora do REST: em imagem nativa
// o Quarkus nao descobre estes records sozinho e o Jackson falha em runtime.
@RegisterForReflection
public record CondicaoEstrategiaCacheDTO(
        Long id,
        String tipoIndicador,
        OperadorComparacaoEnum operador,
        String valor,
        LocalDateTime dateCreated)
        implements Serializable {}
