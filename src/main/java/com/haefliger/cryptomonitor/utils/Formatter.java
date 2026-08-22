package com.haefliger.cryptomonitor.utils;

public final class Formatter {

    private Formatter() {}

    public static String formatInterval(String input) {
        if (input != null && input.matches(".*[a-zA-Z].*")) {
            return input;
        }
        return input == null ? null : input + "m";
    }
}
