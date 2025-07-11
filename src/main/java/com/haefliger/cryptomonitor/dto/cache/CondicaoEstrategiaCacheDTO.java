package com.haefliger.cryptomonitor.dto.cache;


import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Author diego-haefliger
 * Date 6/20/25
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CondicaoEstrategiaCacheDTO implements Serializable  {
    private Long id;
    private String tipoIndicador;
    private OperadorComparacaoEnum operador;
    private String valor;
    private LocalDateTime dateCreated;
}
