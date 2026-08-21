package com.haefliger.cryptomonitor.dto.response.estrategia;

import java.time.LocalDateTime;
import java.util.List;

public record BuscarEstrategiaListaResponse(
        Long id,
        String nome,
        String simbolo,
        String intervalo,
        String operadorLogico,
        boolean ativo,
        boolean permanente,
        LocalDateTime dateCreated,
        LocalDateTime dateLastUpdate,
        List<BuscarEstrategiaCondicaoResponse> condicoes) {
}
