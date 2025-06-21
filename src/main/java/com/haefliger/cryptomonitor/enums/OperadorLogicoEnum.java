package com.haefliger.cryptomonitor.enums;

public enum OperadorLogicoEnum {

    AND,
    OR;

    public static boolean isValid(String value) {
        for (OperadorLogicoEnum op : values()) {
            if (op.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static String valoresValidos() {
        StringBuilder sb = new StringBuilder();
        for (OperadorLogicoEnum op : values()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(op.name());
        }
        return sb.toString();
    }
}

