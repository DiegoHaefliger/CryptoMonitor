package com.haefliger.cryptomonitor.dto.request.estrategia;

import com.haefliger.cryptomonitor.validation.NotEmptyWithFieldMessage;
import lombok.Data;

@Data
public class CondicaoRequest {
    @NotEmptyWithFieldMessage(fieldName = "type")
    private String type;

    @NotEmptyWithFieldMessage(fieldName = "operator")
    private String operator;

    @NotEmptyWithFieldMessage(fieldName = "value")
    private String value;
}
