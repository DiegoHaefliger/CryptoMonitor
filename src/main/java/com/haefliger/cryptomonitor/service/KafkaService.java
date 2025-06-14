package com.haefliger.cryptomonitor.service;


/**
 * Author diego-haefliger
 * Date 12/06/25
 */

public interface KafkaService {

    void sendMessage(String topic, String key, Object obj);

}
