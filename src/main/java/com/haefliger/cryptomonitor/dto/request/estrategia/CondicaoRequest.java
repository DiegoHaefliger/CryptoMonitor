package com.haefliger.cryptomonitor.dto.request.estrategia;

import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.validation.NotEmptyWithFieldMessage;
import com.haefliger.cryptomonitor.validation.OperadorComparacaoValido;
import com.haefliger.cryptomonitor.validation.TipoIndicadorValido;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CondicaoRequest {
    @Schema(description = "Tipo de indicador", example = "RSI")
    @TipoIndicadorValido
    private TipoIndicadorEnum tipoIndicador;

    @Schema(description = "Operação lógica", example = "<")
    @OperadorComparacaoValido
    private String operador;

    @Schema(description = "Valor do operador", example = "30")
    @NotEmptyWithFieldMessage(fieldName = "valor")
    private Integer valor;
}
