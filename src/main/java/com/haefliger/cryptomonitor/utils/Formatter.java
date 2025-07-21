package com.haefliger.cryptomonitor.utils;


import lombok.experimental.UtilityClass;

/**
 * Author diego-haefliger
 * Date 7/19/25
 */

@UtilityClass
public class Formatter {

    public static String formatInterval(String input) {
        if (input != null && input.matches(".*[a-zA-Z].*")) {
            return input;
        }
        return input == null ? null : input + "m";
    }

}
