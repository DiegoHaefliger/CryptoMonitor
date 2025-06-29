package com.haefliger.cryptomonitor.service;


import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;

/**
 * Author diego-haefliger
 * Date 12/06/25
 */

public interface KafkaService {

    void sendMessage(String topic, String key, Object obj);
    void sendMessageEstrategias(TipoIndicadorEnum indicador, String[] parametros);

}
