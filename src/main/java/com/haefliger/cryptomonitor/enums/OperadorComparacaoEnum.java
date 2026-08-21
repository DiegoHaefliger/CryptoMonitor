package com.haefliger.cryptomonitor.enums;

import java.util.function.BiPredicate;

public enum OperadorComparacaoEnum {
    MENOR("<", (a, b) -> a < b),
    MAIOR(">", (a, b) -> a > b),
    MENOR_IGUAL("<=", (a, b) -> a <= b),
    MAIOR_IGUAL(">=", (a, b) -> a >= b),
    IGUAL("=", Double::equals);

    private final String simbolo;
    private final BiPredicate<Double, Double> operador;

    OperadorComparacaoEnum(String simbolo, BiPredicate<Double, Double> operador) {
        this.simbolo = simbolo;
        this.operador = operador;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public boolean comparar(Double valorAlvo, Double valorComparacao) {
        return operador.test(valorAlvo, valorComparacao);
    }

    public static boolean isValid(OperadorComparacaoEnum value) {
        for (OperadorComparacaoEnum op : values()) {
            if (op == value) {
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
