package com.haefliger.cryptomonitor.controller;

import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.EstrategiaResponse;
import com.haefliger.cryptomonitor.exception.dto.ApiErrorResponse;
import com.haefliger.cryptomonitor.service.EstrategiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Estrategia", description = "Endpoints for managing estrategias")
@Validated
@AllArgsConstructor
@RestController
@RequestMapping(value = "/estrategia")
public class EstrategiaController {

    private final EstrategiaService estrategiaService;

    @Operation(summary = "Salva uma nova estratégia", description = "Salva uma nova estratégia",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estratégia salva com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    @PostMapping("")
    public EstrategiaResponse salvarEstrategia(@RequestBody @Valid EstrategiaRequest estrategiaRequest) {

        return estrategiaService.salvarEstrategia(estrategiaRequest);
    }

}
