package com.haefliger.cryptomonitor.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haefliger.cryptomonitor.dto.cache.CondicaoEstrategiaCacheDTO;
import com.haefliger.cryptomonitor.dto.cache.EstrategiaCacheDTO;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@DisplayName(
        "Formato do cache Redis — contrato congelado na F0, agora produzido pelo ObjectMapper do Quarkus")
class RedisCacheFormatoTest {

    private static final String JSON_CONGELADO =
            "[{\"id\":1,\"nome\":\"Bitcoin RSI\",\"simbolo\":\"BTCUSDT\",\"intervalo\":\"60\","
                    + "\"operadorLogico\":\"AND\",\"dateCreated\":\"2026-01-01T10:00:00\",\"dateLastUpdate\":null,"
                    + "\"ativo\":true,\"permanente\":false,"
                    + "\"condicoes\":[{\"id\":10,\"tipoIndicador\":\"RSI\",\"operador\":\"MENOR\",\"valor\":\"30\","
                    + "\"dateCreated\":\"2026-01-01T10:00:00\"}]}]";

    @Inject ObjectMapper objectMapper;

    private static List<EstrategiaCacheDTO> amostra() {
        CondicaoEstrategiaCacheDTO condicao =
                new CondicaoEstrategiaCacheDTO(
                        10L,
                        "RSI",
                        OperadorComparacaoEnum.MENOR,
                        "30",
                        LocalDateTime.parse("2026-01-01T10:00:00"));
        return List.of(
                new EstrategiaCacheDTO(
                        1L,
                        "Bitcoin RSI",
                        "BTCUSDT",
                        "60",
                        "AND",
                        LocalDateTime.parse("2026-01-01T10:00:00"),
                        null,
                        Boolean.TRUE,
                        Boolean.FALSE,
                        List.of(condicao)));
    }

    @Test
    @DisplayName(
            "o JSON gravado é byte a byte o mesmo da versão Spring: data ISO-8601, nulo explícito, enum pelo name()")
    void formatoGravado() throws Exception {
        assertThat(objectMapper.writeValueAsString(amostra())).isEqualTo(JSON_CONGELADO);
    }

    @Test
    @DisplayName(
            "o cache escrito pela versão Spring continua legível — sem \"@class\", direto para o record")
    void leEscritoPelaVersaoAnterior() throws Exception {
        assertThat(JSON_CONGELADO).doesNotContain("@class");

        List<EstrategiaCacheDTO> lido =
                objectMapper.readValue(
                        JSON_CONGELADO, new TypeReference<List<EstrategiaCacheDTO>>() {});

        assertThat(lido)
                .singleElement()
                .satisfies(
                        dto -> {
                            assertThat(dto.id()).isEqualTo(1L);
                            assertThat(dto.ativo()).isTrue();
                            assertThat(dto.dateCreated())
                                    .isEqualTo(LocalDateTime.parse("2026-01-01T10:00:00"));
                            assertThat(dto.condicoes())
                                    .singleElement()
                                    .extracting(CondicaoEstrategiaCacheDTO::operador)
                                    .isEqualTo(OperadorComparacaoEnum.MENOR);
                        });
    }
}
