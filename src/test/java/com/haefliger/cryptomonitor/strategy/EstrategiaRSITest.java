package com.haefliger.cryptomonitor.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.service.KafkaService;
import com.haefliger.cryptomonitor.strategy.domain.PrecoSimboloDomain;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstrategiaRSI — caracterização do comportamento atual")
class EstrategiaRSITest {

    private static final double TOLERANCIA = 1e-9;

    @Mock private KafkaService kafkaService;

    @InjectMocks private EstrategiaRSI estrategiaRSI;

    private static List<PrecoSimboloDomain> serie(double... precos) {
        Instant base = Instant.parse("2026-01-01T00:00:00Z");
        return IntStream.range(0, precos.length)
                .mapToObj(
                        i ->
                                new PrecoSimboloDomain(
                                        BigDecimal.valueOf(precos[i]), base.plusSeconds(60L * i)))
                .toList();
    }

    private static final double[] SERIE_MISTA = {
        100, 102, 101, 103, 105, 104, 106, 108, 107, 109, 111, 110, 112, 114, 113, 115, 117, 116
    };

    @Test
    @DisplayName("série mista de 18 pontos produz os dois últimos RSIs de Wilder")
    void calculaRsiComSuavizacaoDeWilder() {
        List<Double> rsi = estrategiaRSI.calcularRSI(serie(SERIE_MISTA));

        assertThat(rsi).hasSize(2);
        assertThat(rsi.get(0))
                .isCloseTo(81.8005599828, org.assertj.core.data.Offset.offset(TOLERANCIA));
        assertThat(rsi.get(1))
                .isCloseTo(78.2435066479, org.assertj.core.data.Offset.offset(TOLERANCIA));
    }

    @Test
    @DisplayName("histórico menor que 15 pontos devolve [0.0, 0.0] em vez de erro")
    void historicoInsuficienteDevolveZeros() {
        assertThat(estrategiaRSI.calcularRSI(serie(1, 2, 3))).containsExactly(0.0, 0.0);
        assertThat(estrategiaRSI.calcularRSI(Collections.emptyList())).containsExactly(0.0, 0.0);
        assertThat(estrategiaRSI.calcularRSI(null)).containsExactly(0.0, 0.0);
    }

    @Test
    @DisplayName("exatamente 15 pontos gera um único RSI, devolvido duplicado")
    void quinzePontosDuplicamOUnicoValor() {
        double[] quinze = new double[15];
        System.arraycopy(SERIE_MISTA, 0, quinze, 0, 15);

        List<Double> rsi = estrategiaRSI.calcularRSI(serie(quinze));

        assertThat(rsi.get(0)).isEqualTo(rsi.get(1));
        assertThat(rsi.get(0))
                .isCloseTo(78.2608695652, org.assertj.core.data.Offset.offset(TOLERANCIA));
    }

    @Test
    @DisplayName("série só de alta satura em 100; só de queda satura em 0")
    void seriesMonotonicas() {
        double[] alta = new double[20];
        double[] queda = new double[20];
        for (int i = 0; i < 20; i++) {
            alta[i] = 100 + i;
            queda[i] = 200 - i;
        }

        assertThat(estrategiaRSI.calcularRSI(serie(alta))).containsExactly(100.0, 100.0);
        assertThat(estrategiaRSI.calcularRSI(serie(queda))).containsExactly(0.0, 0.0);
    }

    @Test
    @DisplayName("entrada fora de ordem é ordenada por timestamp antes do cálculo")
    void ordenaPorTimestampAntesDeCalcular() {
        List<PrecoSimboloDomain> ordenada = serie(SERIE_MISTA);
        List<PrecoSimboloDomain> embaralhada = new ArrayList<>(ordenada);
        Collections.reverse(embaralhada);

        assertThat(estrategiaRSI.calcularRSI(embaralhada))
                .isEqualTo(estrategiaRSI.calcularRSI(ordenada));
    }

    @Test
    @DisplayName(
            "dispara quando o alvo satisfazia o operador no RSI anterior e deixa de satisfazer no atual")
    void disparaNaTransicaoDoOperador() {
        Estrategia estrategia = estrategiaComCondicao(OperadorComparacaoEnum.MENOR, 80);

        estrategiaRSI.analisar(serie(SERIE_MISTA), "BTCUSDT-60", List.of(estrategia));

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(kafkaService)
                .sendMessageEstrategias(
                        org.mockito.ArgumentMatchers.eq(TipoIndicadorEnum.RSI), captor.capture());
        assertThat(captor.getValue()).containsExactly("BTCUSDT", "< 80", "60m");
    }

    @Test
    @DisplayName("não dispara quando a condição continua satisfeita nos dois RSIs")
    void naoDisparaSemTransicao() {
        Estrategia estrategia = estrategiaComCondicao(OperadorComparacaoEnum.MENOR, 10);

        estrategiaRSI.analisar(serie(SERIE_MISTA), "BTCUSDT-60", List.of(estrategia));

        verifyNoInteractions(kafkaService);
    }

    @Test
    @DisplayName("estratégia sem condição é ignorada, sem erro")
    void estrategiaSemCondicaoEhIgnorada() {
        Estrategia semCondicao = new Estrategia();
        semCondicao.setNome("vazia");
        semCondicao.setCondicoes(List.of());
        Estrategia condicaoNula = new Estrategia();
        condicaoNula.setNome("nula");

        estrategiaRSI.analisar(
                serie(SERIE_MISTA), "BTCUSDT-60", List.of(semCondicao, condicaoNula));

        verify(kafkaService, never())
                .sendMessageEstrategias(
                        org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void nomeDaEstrategia() {
        assertThat(estrategiaRSI.getNome()).isEqualTo("RSI");
    }

    private static Estrategia estrategiaComCondicao(OperadorComparacaoEnum operador, int valor) {
        CondicaoEstrategia condicao = new CondicaoEstrategia();
        condicao.setTipoIndicador(TipoIndicadorEnum.RSI);
        condicao.setOperador(operador);
        condicao.setValor(BigDecimal.valueOf(valor));

        Estrategia estrategia = new Estrategia();
        estrategia.setNome("rsi");
        estrategia.setSimbolo("BTCUSDT");
        estrategia.setCondicoes(List.of(condicao));
        return estrategia;
    }
}
