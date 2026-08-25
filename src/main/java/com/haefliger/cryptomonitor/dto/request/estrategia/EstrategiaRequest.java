package com.haefliger.cryptomonitor.dto.request.estrategia;

import com.haefliger.cryptomonitor.enums.OperadorLogicoEnum;
import com.haefliger.cryptomonitor.validation.IntervaloValido;
import com.haefliger.cryptomonitor.validation.NotEmptyWithFieldMessage;
import com.haefliger.cryptomonitor.validation.OperadorLogicoValido;
import com.haefliger.cryptomonitor.validation.SimboloValido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record EstrategiaRequest(
        @Schema(description = "Nome para a estratégia", example = "Estratégia Bitcoin RSI")
                @NotEmptyWithFieldMessage(fieldName = "nome")
                String nome,
        @Schema(description = "Símbolo", example = "BTCUSDT") @SimboloValido String simbolo,
        @Schema(description = "Tempo de intervalo", example = "1") @IntervaloValido
                String intervalo,
        @Schema(description = "Operação lógica", example = "AND") @OperadorLogicoValido
                OperadorLogicoEnum operadorLogico,
        @Schema(
                        description =
                                "Indica se essa estratégia deve ser executada permanentemente",
                        example = "true")
                @NotNull(message = "Campo 'permanente' não pode ser vazio")
                Boolean permanente,
        @Schema(description = "Lista de condições")
                @NotEmptyWithFieldMessage(fieldName = "conditions")
                @NotEmpty
                @Valid
                List<CondicaoRequest> condicoes) {}
