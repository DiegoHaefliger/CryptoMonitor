package com.haefliger.cryptomonitor.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OperadorComparacaoEnum — caracterização do comportamento atual")
class OperadorComparacaoEnumTest {

    @Test
    @DisplayName("comparar(a, b) avalia a OPERADOR b, com o alvo do lado esquerdo")
    void ordemDosArgumentosEhAlvoDepoisComparacao() {
        assertThat(OperadorComparacaoEnum.MENOR.comparar(30.0, 70.0)).isTrue();
        assertThat(OperadorComparacaoEnum.MENOR.comparar(70.0, 30.0)).isFalse();
        assertThat(OperadorComparacaoEnum.MAIOR.comparar(70.0, 30.0)).isTrue();
        assertThat(OperadorComparacaoEnum.MAIOR.comparar(30.0, 70.0)).isFalse();
    }

    @Test
    void limitesDosOperadoresInclusivos() {
        assertThat(OperadorComparacaoEnum.MENOR_IGUAL.comparar(30.0, 30.0)).isTrue();
        assertThat(OperadorComparacaoEnum.MAIOR_IGUAL.comparar(30.0, 30.0)).isTrue();
        assertThat(OperadorComparacaoEnum.MENOR.comparar(30.0, 30.0)).isFalse();
        assertThat(OperadorComparacaoEnum.MAIOR.comparar(30.0, 30.0)).isFalse();
    }

    @Test
    @DisplayName("IGUAL usa Double::equals, então compara identidade de valor de ponto flutuante")
    void igualUsaEqualsDeDouble() {
        assertThat(OperadorComparacaoEnum.IGUAL.comparar(30.0, 30.0)).isTrue();
        assertThat(OperadorComparacaoEnum.IGUAL.comparar(30.0, 30.000000001)).isFalse();
        assertThat(OperadorComparacaoEnum.IGUAL.comparar(0.1 + 0.2, 0.3)).isFalse();
    }

    @Test
    void simboloDeCadaOperador() {
        assertThat(OperadorComparacaoEnum.MENOR.getSimbolo()).isEqualTo("<");
        assertThat(OperadorComparacaoEnum.MAIOR.getSimbolo()).isEqualTo(">");
        assertThat(OperadorComparacaoEnum.MENOR_IGUAL.getSimbolo()).isEqualTo("<=");
        assertThat(OperadorComparacaoEnum.MAIOR_IGUAL.getSimbolo()).isEqualTo(">=");
        assertThat(OperadorComparacaoEnum.IGUAL.getSimbolo()).isEqualTo("=");
    }

    @Test
    void valoresValidosListaOsSimbolosNaOrdemDeDeclaracao() {
        assertThat(OperadorComparacaoEnum.valoresValidos()).isEqualTo("<, >, <=, >=, =");
    }

    @Test
    @DisplayName("isValid(null) é false; qualquer constante é true")
    void isValid() {
        assertThat(OperadorComparacaoEnum.isValid(null)).isFalse();
        assertThat(OperadorComparacaoEnum.isValid(OperadorComparacaoEnum.MENOR)).isTrue();
    }
}
