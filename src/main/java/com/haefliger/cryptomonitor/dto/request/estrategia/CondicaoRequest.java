package com.haefliger.cryptomonitor.dto.request.estrategia;

import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.validation.NotEmptyWithFieldMessage;
import com.haefliger.cryptomonitor.validation.OperadorComparacaoValido;
import com.haefliger.cryptomonitor.validation.TipoIndicadorValido;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record CondicaoRequest(
        @Schema(description = "Tipo de indicador", example = "RSI") @TipoIndicadorValido
                TipoIndicadorEnum tipoIndicador,
        @Schema(description = "Operação lógica", example = "<") @OperadorComparacaoValido
                OperadorComparacaoEnum operador,
        @Schema(description = "Valor do operador", example = "30")
                @NotEmptyWithFieldMessage(fieldName = "valor")
                Integer valor) {}
