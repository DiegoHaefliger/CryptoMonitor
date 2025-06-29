package com.haefliger.cryptomonitor.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Author diego-haefliger
 * Date 12/06/25
 */

@Getter
@AllArgsConstructor
public enum KafkaEnum {

    ESTRATEGIA("strategy");

    private final String topic;

    public static KafkaEnum fromTopic(String topic) {
        return Arrays.stream(values())
                .filter(value -> value.getTopic().equals(topic))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown topic: " + topic));
    }

}
