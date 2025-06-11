package com.haefliger.cryptomonitor.dto.response.estrategia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author diego-haefliger
 * @since 6/9/25 19:32
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuscarEstrategiaResponse {

    private List<BuscarEstrategiaListaResponse> estrategias;

}
