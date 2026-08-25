package com.haefliger.cryptomonitor.enums;

public enum KafkaEnum {
    ESTRATEGIA("strategy");

    private final String topic;

    KafkaEnum(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}
