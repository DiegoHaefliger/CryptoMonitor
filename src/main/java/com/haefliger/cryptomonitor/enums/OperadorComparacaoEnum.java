package com.haefliger.cryptomonitor.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiPredicate;

@Getter
@AllArgsConstructor
public enum OperadorComparacaoEnum {
    MENOR("<", (a, b) -> a < b),
    MAIOR(">", (a, b) -> a > b),
    MENOR_IGUAL("<=", (a, b) -> a <= b),
    MAIOR_IGUAL(">=", (a, b) -> a >= b),
    IGUAL("=", Double::equals);

    private final String simbolo;
    private final BiPredicate<Double, Double> operador;

    public boolean comparar(Double valorAlvo, Double valorComparacao) {
        return operador.test(valorAlvo, valorComparacao);
    }

    public static boolean isValid(OperadorComparacaoEnum value) {
        for (OperadorComparacaoEnum op : values()) {
            if (op ==  value) {
                return true;
            }
        }
        return false;
    }

    public static String valoresValidos() {
        StringBuilder sb = new StringBuilder();
        for (OperadorComparacaoEnum op : values()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(op.getSimbolo());
        }
        return sb.toString();
    }
}
