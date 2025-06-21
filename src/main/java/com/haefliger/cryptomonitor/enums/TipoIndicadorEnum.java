package com.haefliger.cryptomonitor.enums;

public enum TipoIndicadorEnum {

    PRECO,
    RSI;

    public static boolean isValid(String value) {
        for (TipoIndicadorEnum tipo : values()) {
            if (tipo.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static String valoresValidos() {
        StringBuilder sb = new StringBuilder();
        for (TipoIndicadorEnum tipo : values()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(tipo.name());
        }
        return sb.toString();
    }
}

