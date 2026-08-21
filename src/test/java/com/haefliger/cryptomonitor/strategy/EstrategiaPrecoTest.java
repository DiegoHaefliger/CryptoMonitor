package com.haefliger.cryptomonitor.strategy;

import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.service.KafkaService;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstrategiaPreco — caracterização do comportamento atual")
class EstrategiaPrecoTest {

    @Mock
    private KafkaService kafkaService;

    @InjectMocks
    private EstrategiaPreco estrategiaPreco;

    private static List<PrecoSimboloDomain> serie(String... precos) {
        Instant base = Instant.parse("2026-01-01T00:00:00Z");
        return IntStream.range(0, precos.length)
                .mapToObj(i -> PrecoSimboloDomain.builder()
                        .price(new BigDecimal(precos[i]))
                        .timestamp(base.plusSeconds(60L * i))
                        .build())
                .toList();
    }

    private static Estrategia estrategiaComAlvo(String alvo) {
        CondicaoEstrategia condicao = CondicaoEstrategia.builder()
                .tipoIndicador(TipoIndicadorEnum.PRECO)
                .operador(OperadorComparacaoEnum.MAIOR)
                .valor(new BigDecimal(alvo))
                .build();
        return Estrategia.builder().nome("preco").simbolo("BTCUSDT").condicoes(List.of(condicao)).build();
    }

    @Test
    @DisplayName("dispara ao cruzar o alvo para cima")
    void disparaNoCruzamentoParaCima() {
        estrategiaPreco.analisar(serie("99", "101"), "BTCUSDT-60", List.of(estrategiaComAlvo("100")));

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(kafkaService).sendMessageEstrategias(eq(TipoIndicadorEnum.PRECO), captor.capture());
        assertThat(captor.getValue()).containsExactly("BTCUSDT", "101");
    }

    @Test
    @DisplayName("dispara ao cruzar o alvo para baixo")
    void disparaNoCruzamentoParaBaixo() {
        estrategiaPreco.analisar(serie("101", "99"), "BTCUSDT-60", List.of(estrategiaComAlvo("100")));

        verify(kafkaService).sendMessageEstrategias(eq(TipoIndicadorEnum.PRECO), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("não dispara sem cruzamento, mesmo com preço acima do alvo nos dois pontos")
    void naoDisparaSemCruzamento() {
        estrategiaPreco.analisar(serie("101", "102"), "BTCUSDT-60", List.of(estrategiaComAlvo("100")));

        verifyNoInteractions(kafkaService);
    }

    @Test
    @DisplayName("preço encostando exatamente no alvo não conta como cruzamento")
    void alvoExatoNaoDispara() {
        estrategiaPreco.analisar(serie("99", "100"), "BTCUSDT-60", List.of(estrategiaComAlvo("100")));

        verifyNoInteractions(kafkaService);
    }

    @Test
    @DisplayName("só os dois últimos pontos importam, o resto do histórico é ignorado")
    void usaApenasOsDoisUltimosPontos() {
        estrategiaPreco.analisar(serie("50", "60", "70", "99", "101"), "BTCUSDT-60", List.of(estrategiaComAlvo("100")));

        verify(kafkaService).sendMessageEstrategias(eq(TipoIndicadorEnum.PRECO), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("histórico com menos de 2 pontos, nulo, ou lista de estratégias vazia: sai sem fazer nada")
    void entradasInsuficientes() {
        Estrategia estrategia = estrategiaComAlvo("100");

        estrategiaPreco.analisar(serie("99"), "BTCUSDT-60", List.of(estrategia));
        estrategiaPreco.analisar(null, "BTCUSDT-60", List.of(estrategia));
        estrategiaPreco.analisar(serie("99", "101"), "BTCUSDT-60", Collections.emptyList());
        estrategiaPreco.analisar(serie("99", "101"), "BTCUSDT-60", null);

        verifyNoInteractions(kafkaService);
    }

    @Test
    @DisplayName("estratégia com lista de condições vazia estoura IndexOutOfBounds — sem guarda, diferente da EstrategiaRSI")
    void condicaoVaziaEstoura() {
        Estrategia semCondicao = Estrategia.builder().nome("vazia").condicoes(List.of()).build();

        assertThatThrownBy(() -> estrategiaPreco.analisar(serie("99", "101"), "BTCUSDT-60", List.of(semCondicao)))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void nomeDaEstrategia() {
        assertThat(estrategiaPreco.getNome()).isEqualTo("PRECO");
    }
}
