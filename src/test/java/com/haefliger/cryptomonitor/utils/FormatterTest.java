package com.haefliger.cryptomonitor.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Formatter — caracterização do comportamento atual")
class FormatterTest {

    @ParameterizedTest
    @CsvSource({"1,1m", "5,5m", "15,15m", "60,60m", "240,240m"})
    void acrescentaMinutoQuandoIntervaloEhSoNumero(String entrada, String esperado) {
        assertThat(Formatter.formatInterval(entrada)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource({"D,D", "W,W", "M,M", "1h,1h"})
    void devolveIntactoQuandoIntervaloContemLetra(String entrada, String esperado) {
        assertThat(Formatter.formatInterval(entrada)).isEqualTo(esperado);
    }

    @Test
    void devolveNuloQuandoEntradaEhNula() {
        assertThat(Formatter.formatInterval(null)).isNull();
    }

    @Test
    @DisplayName("string vazia vira \"m\" — comportamento atual, não necessariamente desejado")
    void stringVaziaViraSufixoSozinho() {
        assertThat(Formatter.formatInterval("")).isEqualTo("m");
    }
}
