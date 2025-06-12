package com.haefliger.cryptomonitor.dto.request.estrategia;

import com.haefliger.cryptomonitor.validation.NotEmptyWithFieldMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EstrategiaRequest {
    @Schema(description = "Nome para a estratégia", example = "Estratégia Bitcoin RSI")
    @NotEmptyWithFieldMessage(fieldName = "nome")
    private String nome;

    @Schema(description = "Símbolo", example = "BTCUSDT")
    @NotEmptyWithFieldMessage(fieldName = "simbolo")
    private String simbolo;

    @Schema(description = "Tempo de intervalo", example = "1h")
    @NotEmptyWithFieldMessage(fieldName = "intervalo")
    private String intervalo;

    @Schema(description = "Operação lógica", example = "AND")
    @NotEmptyWithFieldMessage(fieldName = "operadorLogico")
    private String operadorLogico;

    @Schema(description = "Indica se essa estratégia deve ser executada permanentemente", example = "true")
    @NotNull(message = "Campo 'permanente' não pode ser vazio")
    private Boolean permanente;

    @Schema(description = "Lista de condições")
    @NotEmptyWithFieldMessage(fieldName = "conditions")
    @NotEmpty
    @Valid
    private List<CondicaoRequest> condicoes;
}
