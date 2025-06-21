package com.haefliger.cryptomonitor.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IntervaloEnum {
    UM("1"),
    CINCO("5"),
    QUINZE("15"),
    TRINTA("30"),
    SESSENTA("60"),
    CENTO_VINTE("120"),
    CENTO_OITENTA("180"),
    DUZENTOS_QUARENTA("240"),
    TREZENTOS_SESSENTA("360"),
    SETECENTOS_VINTE("720"),
    DIARIO("D"),
    SEMANAL("W"),
    MENSAL("M");

    private final String valor;

    public static boolean isValid(String value) {
        for (IntervaloEnum intervalo : values()) {
            if (intervalo.getValor().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static String valoresValidos() {
        StringBuilder sb = new StringBuilder();
        for (IntervaloEnum intervalo : values()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(intervalo.getValor());
        }
        return sb.toString();
    }

}

