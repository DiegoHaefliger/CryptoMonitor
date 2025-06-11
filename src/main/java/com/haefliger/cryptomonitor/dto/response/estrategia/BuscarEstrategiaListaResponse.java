package com.haefliger.cryptomonitor.dto.response.estrategia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author diego-haefliger
 * @since 6/9/25 19:32
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuscarEstrategiaListaResponse {

    private Long id;
    private String nome;
    private String simbolo;
    private String intervalo;
    private String operadorLogico;
    private boolean ativo;
    private LocalDateTime dateCreated;
    private List<BuscarEstrategiaCondicaoResponse> condicoes;
    
}
