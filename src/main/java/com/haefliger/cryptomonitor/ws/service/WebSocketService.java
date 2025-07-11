package com.haefliger.cryptomonitor.ws.service;


import java.util.List;
import java.util.Map;

/**
 * Author diego-haefliger
 * Date 15/06/25
 */

public interface WebSocketService {

    void conect(Map<String, List<String>> symbolIntervals);
    void disconnect();
    void atualizaEstrategiasWS();
}
