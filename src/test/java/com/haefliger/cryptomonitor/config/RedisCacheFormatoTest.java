package com.haefliger.cryptomonitor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haefliger.cryptomonitor.dto.cache.CondicaoEstrategiaCacheDTO;
import com.haefliger.cryptomonitor.dto.cache.EstrategiaCacheDTO;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("Formato do cache Redis — contrato congelado antes da migração")
class RedisCacheFormatoTest {

    private static final String JSON_ESPERADO =
            "[{\"id\":1,\"nome\":\"Bitcoin RSI\",\"simbolo\":\"BTCUSDT\",\"intervalo\":\"60\","
            + "\"operadorLogico\":\"AND\",\"dateCreated\":\"2026-01-01T10:00:00\",\"dateLastUpdate\":null,"
            + "\"ativo\":true,\"permanente\":false,"
            + "\"condicoes\":[{\"id\":10,\"tipoIndicador\":\"RSI\",\"operador\":\"MENOR\",\"valor\":\"30\","
            + "\"dateCreated\":\"2026-01-01T10:00:00\"}]}]";

    private static RedisSerializer<?> serializadorDeProducao() {
        RedisTemplate<String, Object> template =
                new RedisConfig().redisTemplate(mock(RedisConnectionFactory.class));
        return template.getValueSerializer();
    }

    private static List<EstrategiaCacheDTO> amostra() {
        CondicaoEstrategiaCacheDTO condicao = CondicaoEstrategiaCacheDTO.builder()
                .id(10L)
                .tipoIndicador("RSI")
                .operador(OperadorComparacaoEnum.MENOR)
                .valor("30")
                .dateCreated(LocalDateTime.parse("2026-01-01T10:00:00"))
                .build();
        return List.of(EstrategiaCacheDTO.builder()
                .id(1L)
                .nome("Bitcoin RSI")
                .simbolo("BTCUSDT")
                .intervalo("60")
                .operadorLogico("AND")
                .dateCreated(LocalDateTime.parse("2026-01-01T10:00:00"))
                .dateLastUpdate(null)
                .ativo(Boolean.TRUE)
                .permanente(Boolean.FALSE)
                .condicoes(List.of(condicao))
                .build());
    }

    @Test
    @DisplayName("bytes gravados no Redis: JSON puro, data ISO-8601, nulo explícito, enum pelo name()")
    void formatoGravado() {
        @SuppressWarnings("unchecked")
        RedisSerializer<Object> serializer = (RedisSerializer<Object>) serializadorDeProducao();

        String json = new String(serializer.serialize(amostra()), StandardCharsets.UTF_8);

        assertThat(json).isEqualTo(JSON_ESPERADO);
    }

    @Test
    @DisplayName("sem \"@class\" no JSON, a leitura volta como LinkedHashMap — é por isso que RedisServiceImpl reconverte")
    void leituraVoltaSemTipo() {
        @SuppressWarnings("unchecked")
        RedisSerializer<Object> serializer = (RedisSerializer<Object>) serializadorDeProducao();

        assertThat(new String(serializer.serialize(amostra()), StandardCharsets.UTF_8)).doesNotContain("@class");

        Object lido = serializer.deserialize(JSON_ESPERADO.getBytes(StandardCharsets.UTF_8));

        assertThat(lido).isInstanceOf(List.class);
        assertThat((List<?>) lido).first().isInstanceOf(LinkedHashMap.class);
    }

    @Test
    @DisplayName("o JSON congelado ainda converte para o DTO — é o caminho que o RedisServiceImpl usa")
    void jsonCongeladoConverteParaDto() throws Exception {
        List<?> cru = new ObjectMapper().readValue(JSON_ESPERADO, List.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        EstrategiaCacheDTO dto = mapper.convertValue(cru.get(0), EstrategiaCacheDTO.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getAtivo()).isTrue();
        assertThat(dto.getDateCreated()).isEqualTo(LocalDateTime.parse("2026-01-01T10:00:00"));
        assertThat(dto.getCondicoes()).singleElement()
                .extracting(CondicaoEstrategiaCacheDTO::getOperador)
                .isEqualTo(OperadorComparacaoEnum.MENOR);
    }
}
