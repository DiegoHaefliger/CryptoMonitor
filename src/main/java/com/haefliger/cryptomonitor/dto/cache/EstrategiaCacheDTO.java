package com.haefliger.cryptomonitor.dto.cache;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
