package com.haefliger.cryptomonitor.controller;

import com.haefliger.cryptomonitor.dto.request.estrategia.CondicaoRequest;
import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaCondicaoResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaListaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.SalvarEstrategiaResponse;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.OperadorLogicoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.service.EstrategiaService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("Contrato HTTP de /estrategia — caracterização do formato atual")
class EstrategiaControllerTest {

    @InjectMock
    EstrategiaService estrategiaService;

    private static EstrategiaRequest requestValida() {
        CondicaoRequest condicao =
                new CondicaoRequest(TipoIndicadorEnum.RSI, OperadorComparacaoEnum.MENOR, 30);
        return new EstrategiaRequest("Bitcoin RSI", "BTCUSDT", "60",
                OperadorLogicoEnum.AND, Boolean.FALSE, List.of(condicao));
    }

    @Test
    @DisplayName("POST devolve 201 com o id embrulhado em {\"dados\": {...}}")
    void salvarEstrategia() {
        when(estrategiaService.salvarEstrategia(any())).thenReturn(new SalvarEstrategiaResponse(7L));

        given().contentType(ContentType.JSON).body(requestValida())
                .when().post("/estrategia")
                .then().statusCode(201)
                .body("dados.id", equalTo(7));
    }

    @Test
    @DisplayName("GET devolve 200 com a lista embrulhada em {\"dados\": {\"estrategias\": [...]}}")
    void buscarEstrategia() {
        BuscarEstrategiaListaResponse item = new BuscarEstrategiaListaResponse(
                1L, "Bitcoin RSI", "BTCUSDT", "60", "AND", true, false,
                LocalDateTime.parse("2026-01-01T10:00:00"), null,
                List.of(new BuscarEstrategiaCondicaoResponse("RSI", "MENOR", "30")));
        when(estrategiaService.buscarEstrategia(null))
                .thenReturn(new BuscarEstrategiaResponse(List.of(item)));

        given().when().get("/estrategia")
                .then().statusCode(200)
                .body("dados.estrategias[0].id", equalTo(1))
                .body("dados.estrategias[0].nome", equalTo("Bitcoin RSI"))
                .body("dados.estrategias[0].ativo", equalTo(true))
                .body("dados.estrategias[0].dateCreated", equalTo("2026-01-01T10:00:00"))
                .body("dados.estrategias[0].dateLastUpdate", nullValue())
                .body("dados.estrategias[0].condicoes[0].tipoIndicador", equalTo("RSI"));
    }

    @Test
    void buscarEstrategiaRepassaFiltroAtivo() {
        when(estrategiaService.buscarEstrategia(Boolean.TRUE))
                .thenReturn(new BuscarEstrategiaResponse(List.of()));

        given().queryParam("ativo", true)
                .when().get("/estrategia")
                .then().statusCode(200);

        verify(estrategiaService).buscarEstrategia(Boolean.TRUE);
    }

    @Test
    void deletarDevolve204SemCorpo() {
        given().queryParam("id", 5)
                .when().delete("/estrategia")
                .then().statusCode(204);

        verify(estrategiaService).deletarEstrategia(5L);
    }

    @Test
    void statusDevolve204SemCorpo() {
        given().queryParam("id", 5).queryParam("ativo", false).queryParam("permanente", true)
                .when().put("/estrategia/status")
                .then().statusCode(204);

        verify(estrategiaService).statusEstrategia(5L, Boolean.FALSE, Boolean.TRUE);
    }

    @Test
    @DisplayName("payload inválido devolve 400 no formato do GlobalExceptionHandler")
    void payloadInvalidoDevolve400() {
        EstrategiaRequest invalida = new EstrategiaRequest("", "BTCUSDT", "7",
                OperadorLogicoEnum.AND, Boolean.FALSE, requestValida().condicoes());

        given().contentType(ContentType.JSON).body(invalida)
                .when().post("/estrategia")
                .then().statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                // inclui o root-path, igual ao getRequestURI() do Spring em producao
                .body("path", equalTo("/crypto-monitor/estrategia"))
                .body("timestamp", org.hamcrest.Matchers.notNullValue())
                .body("errors", hasSize(2))
                .body("errors.defaultMessage", hasItem("Campo 'nome' não pode ser vazio"));
    }

    @Test
    @DisplayName("DELETE sem id devolve 400, não 500 — @QueryParam sozinho seria opcional, diferente do @RequestParam do Spring")
    void deletarSemIdDevolve400() {
        given().when().delete("/estrategia")
                .then().statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("errors.defaultMessage", hasItem("Campo 'id' não pode ser vazio"));
    }

    @Test
    @DisplayName("PUT /status sem parâmetros aponta os três que faltaram")
    void statusSemParametrosDevolve400() {
        given().when().put("/estrategia/status")
                .then().statusCode(400)
                .body("errors.defaultMessage", hasItem("Campo 'id' não pode ser vazio"))
                .body("errors.defaultMessage", hasItem("Campo 'ativo' não pode ser vazio"))
                .body("errors.defaultMessage", hasItem("Campo 'permanente' não pode ser vazio"));
    }
}
