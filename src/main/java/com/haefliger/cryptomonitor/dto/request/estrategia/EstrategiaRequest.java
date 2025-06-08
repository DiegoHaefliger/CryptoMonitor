package com.haefliger.cryptomonitor.dto.request.estrategia;

import com.haefliger.cryptomonitor.validation.NotEmptyWithFieldMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EstrategiaRequest {
    @NotEmptyWithFieldMessage(fieldName = "symbol")
    private String symbol;

    @NotEmptyWithFieldMessage(fieldName = "interval")
    private String interval;

    @NotEmptyWithFieldMessage(fieldName = "conditions")
    @NotEmpty
    @Valid
    private List<CondicaoRequest> conditions;

    @NotEmptyWithFieldMessage(fieldName = "logic")
    private String logic;
}
