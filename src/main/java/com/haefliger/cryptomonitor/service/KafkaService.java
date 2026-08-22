package com.haefliger.cryptomonitor.service;

import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;

public interface KafkaService {

    void sendMessageEstrategias(TipoIndicadorEnum indicador, String[] parametros);
}
