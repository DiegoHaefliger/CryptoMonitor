package com.haefliger.cryptomonitor.controller;

import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.SalvarEstrategiaResponse;
import com.haefliger.cryptomonitor.exception.dto.ApiErrorResponse;
import com.haefliger.cryptomonitor.service.EstrategiaService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.ResponseStatus;

@Tag(name = "Estratégia", description = "Endpoints para manipular estratégias de monitoramento de criptomoedas")
@Path("/estrategia")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EstrategiaController {

    private static final String ERRO_REQUISICAO_INVALIDA = "Requisição inválida";

    private final EstrategiaService estrategiaService;

    EstrategiaController(EstrategiaService estrategiaService) {
        this.estrategiaService = estrategiaService;
    }

    @POST
    @ResponseStatus(201)
    @Operation(summary = "Salva uma nova estratégia", description = "Salva uma nova estratégia")
    @APIResponse(responseCode = "201", description = "Estratégia salva com sucesso")
    @APIResponse(responseCode = "400", description = ERRO_REQUISICAO_INVALIDA,
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ApiErrorResponse.class)))
    public SalvarEstrategiaResponse salvarEstrategia(@Valid EstrategiaRequest estrategiaRequest) {
        return estrategiaService.salvarEstrategia(estrategiaRequest);
    }

    @GET
    @ResponseStatus(200)
    @Operation(summary = "Buscar estratégia", description = "Buscar estratégia existente")
    @APIResponse(responseCode = "200", description = "Estratégia retornada com sucesso")
    @APIResponse(responseCode = "400", description = ERRO_REQUISICAO_INVALIDA,
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ApiErrorResponse.class)))
    public BuscarEstrategiaResponse buscarEstrategia(@QueryParam("ativo") Boolean ativo) {
        return estrategiaService.buscarEstrategia(ativo);
    }

    @DELETE
    @ResponseStatus(204)
    @Operation(summary = "Deletar estratégia", description = "Deleta uma estratégia existente")
    @APIResponse(responseCode = "204", description = "Estratégia deletada com sucesso")
    @APIResponse(responseCode = "400", description = ERRO_REQUISICAO_INVALIDA,
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ApiErrorResponse.class)))
    public void deletarEstrategia(@QueryParam("id") Long id) {
        estrategiaService.deletarEstrategia(id);
    }

    @PUT
    @Path("/status")
    @ResponseStatus(204)
    @Operation(summary = "Ativar/Desativar estratégia", description = "Ativar/Desativar uma estratégia existente")
    @APIResponse(responseCode = "204", description = "Estratégia Ativada/Desativada com sucesso")
    @APIResponse(responseCode = "400", description = ERRO_REQUISICAO_INVALIDA,
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ApiErrorResponse.class)))
    public void statusEstrategia(@QueryParam("id") Long id,
                                 @QueryParam("ativo") Boolean ativo,
                                 @QueryParam("permanente") Boolean permanente) {
        estrategiaService.statusEstrategia(id, ativo, permanente);
    }

}
