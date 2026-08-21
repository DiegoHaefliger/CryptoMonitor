package com.haefliger.cryptomonitor.enums;

import java.util.Arrays;

public enum KafkaEnum {

    ESTRATEGIA("strategy");

    private final String topic;

    KafkaEnum(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public static KafkaEnum fromTopic(String topic) {
        return Arrays.stream(values())
                .filter(value -> value.getTopic().equals(topic))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown topic: " + topic));
    }

}
