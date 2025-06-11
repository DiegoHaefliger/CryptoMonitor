package com.haefliger.cryptomonitor.controller;

import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.SalvarEstrategiaResponse;
import com.haefliger.cryptomonitor.exception.dto.ApiErrorResponse;
import com.haefliger.cryptomonitor.service.EstrategiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Estratégia", description = "Endpoints para manipular estratégias de monitoramento de criptomoedas")
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
    @ResponseStatus(HttpStatus.CREATED)
    public SalvarEstrategiaResponse salvarEstrategia(@RequestBody @Valid EstrategiaRequest estrategiaRequest) {

        return estrategiaService.salvarEstrategia(estrategiaRequest);
    }

    @Operation(summary = "Buscar estratégia", description = "Buscar estratégia existente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estratégia retornada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public BuscarEstrategiaResponse buscarEstrategia(@RequestParam(required = false) Boolean ativo) {
        return estrategiaService.buscarEstrategia(ativo);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar estratégia", description = "Deleta uma estratégia existente",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Estratégia deletada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    public void deletarEstrategia(@RequestParam Long id) {
        estrategiaService.deletarEstrategia(id);
    }

}
