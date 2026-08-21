package com.haefliger.cryptomonitor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstrategiaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Contrato HTTP de /estrategia — caracterização do formato atual")
class EstrategiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EstrategiaService estrategiaService;

    private static EstrategiaRequest requestValida() {
        CondicaoRequest condicao = new CondicaoRequest();
        condicao.setTipoIndicador(TipoIndicadorEnum.RSI);
        condicao.setOperador(OperadorComparacaoEnum.MENOR);
        condicao.setValor(30);
        return EstrategiaRequest.builder()
                .nome("Bitcoin RSI")
                .simbolo("BTCUSDT")
                .intervalo("60")
                .operadorLogico(OperadorLogicoEnum.AND)
                .permanente(Boolean.FALSE)
                .condicoes(List.of(condicao))
                .build();
    }

    @Test
    @DisplayName("POST devolve 201 com o id embrulhado em {\"dados\": {...}}")
    void salvarEstrategia() throws Exception {
        SalvarEstrategiaResponse resposta = new SalvarEstrategiaResponse();
        resposta.setId(7L);
        when(estrategiaService.salvarEstrategia(any())).thenReturn(resposta);

        mockMvc.perform(post("/estrategia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValida())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.id").value(7));
    }

    @Test
    @DisplayName("GET devolve 200 com a lista embrulhada em {\"dados\": {\"estrategias\": [...]}}")
    void buscarEstrategia() throws Exception {
        BuscarEstrategiaListaResponse item = BuscarEstrategiaListaResponse.builder()
                .id(1L)
                .nome("Bitcoin RSI")
                .simbolo("BTCUSDT")
                .intervalo("60")
                .operadorLogico("AND")
                .ativo(true)
                .permanente(false)
                .dateCreated(LocalDateTime.parse("2026-01-01T10:00:00"))
                .condicoes(List.of(BuscarEstrategiaCondicaoResponse.builder()
                        .tipoIndicador("RSI")
                        .operador("MENOR")
                        .valor("30")
                        .build()))
                .build();
        when(estrategiaService.buscarEstrategia(null))
                .thenReturn(BuscarEstrategiaResponse.builder().estrategias(List.of(item)).build());

        mockMvc.perform(get("/estrategia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.estrategias[0].id").value(1))
                .andExpect(jsonPath("$.dados.estrategias[0].nome").value("Bitcoin RSI"))
                .andExpect(jsonPath("$.dados.estrategias[0].ativo").value(true))
                .andExpect(jsonPath("$.dados.estrategias[0].dateCreated").value("2026-01-01T10:00:00"))
                .andExpect(jsonPath("$.dados.estrategias[0].dateLastUpdate").doesNotExist())
                .andExpect(jsonPath("$.dados.estrategias[0].condicoes[0].tipoIndicador").value("RSI"));
    }

    @Test
    void buscarEstrategiaRepassaFiltroAtivo() throws Exception {
        when(estrategiaService.buscarEstrategia(Boolean.TRUE))
                .thenReturn(BuscarEstrategiaResponse.builder().estrategias(List.of()).build());

        mockMvc.perform(get("/estrategia").param("ativo", "true"))
                .andExpect(status().isOk());

        verify(estrategiaService).buscarEstrategia(Boolean.TRUE);
    }

    @Test
    void deletarDevolve204SemCorpo() throws Exception {
        mockMvc.perform(delete("/estrategia").param("id", "5"))
                .andExpect(status().isNoContent());

        verify(estrategiaService).deletarEstrategia(5L);
    }

    @Test
    void statusDevolve204SemCorpo() throws Exception {
        mockMvc.perform(put("/estrategia/status")
                        .param("id", "5")
                        .param("ativo", "false")
                        .param("permanente", "true"))
                .andExpect(status().isNoContent());

        verify(estrategiaService).statusEstrategia(eq(5L), eq(Boolean.FALSE), eq(Boolean.TRUE));
    }

    @Test
    @DisplayName("payload inválido devolve 400 no formato do GlobalExceptionHandler")
    void payloadInvalidoDevolve400() throws Exception {
        EstrategiaRequest invalida = requestValida();
        invalida.setNome("");
        invalida.setIntervalo("7");

        mockMvc.perform(post("/estrategia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/estrategia"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*].defaultMessage",
                        org.hamcrest.Matchers.hasItem("Campo 'nome' não pode ser vazio")));
    }
}
