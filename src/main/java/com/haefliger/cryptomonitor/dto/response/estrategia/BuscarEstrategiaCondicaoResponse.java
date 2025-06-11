package com.haefliger.cryptomonitor.dto.response.estrategia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author diego-haefliger
 * @since 6/9/25 19:34
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuscarEstrategiaCondicaoResponse {

    private String tipoIndicador;
    private String operador;
    private String valor;

}
