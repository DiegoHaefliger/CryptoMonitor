package com.haefliger.cryptomonitor.dto.request.estrategia;

import com.haefliger.cryptomonitor.validation.NotEmptyWithFieldMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CondicaoRequest {
    @Schema(description = "Operação lógica", example = "RSI")
    @NotEmptyWithFieldMessage(fieldName = "tipoIndicador")
    private String tipoIndicador;

    @Schema(description = "Operação lógica", example = "<")
    @NotEmptyWithFieldMessage(fieldName = "operador")
    private String operador;

    @Schema(description = "Operação lógica", example = "30")
    @NotEmptyWithFieldMessage(fieldName = "valor")
    private String valor;
}
