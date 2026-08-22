package com.haefliger.cryptomonitor.dto.cache;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

// Serializado direto pelo ObjectMapper (cache Redis), fora do REST: em imagem nativa
// o Quarkus nao descobre estes records sozinho e o Jackson falha em runtime.
@RegisterForReflection
public record EstrategiaCacheDTO(
        Long id,
        String nome,
        String simbolo,
        String intervalo,
        String operadorLogico,
        LocalDateTime dateCreated,
        LocalDateTime dateLastUpdate,
        Boolean ativo,
        Boolean permanente,
        List<CondicaoEstrategiaCacheDTO> condicoes)
        implements Serializable {}
