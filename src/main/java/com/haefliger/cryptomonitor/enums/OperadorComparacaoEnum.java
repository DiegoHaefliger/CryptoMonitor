package com.haefliger.cryptomonitor.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperadorComparacaoEnum {
    MENOR("<"),
    MAIOR(">"),
    MENOR_IGUAL("<="),
    MAIOR_IGUAL(">="),
    IGUAL("=");

    private final String simbolo;

    public static boolean isValid(String value) {
        for (OperadorComparacaoEnum op : values()) {
            if (op.getSimbolo().equals(value)) {
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

