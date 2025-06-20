package com.haefliger.cryptomonitor.service;


import com.haefliger.cryptomonitor.dto.cache.EstrategiaCacheDTO;
import com.haefliger.cryptomonitor.entity.Estrategia;

import java.util.List;

/**
 * Author diego-haefliger
 * Date 6/17/25
 */
public interface RedisService {

    void salvarEstrategiasAtivasRedis(List<EstrategiaCacheDTO> estrategias);

    List<Estrategia> buscarEstrategiasAtivasRedis();

    void excluirEstrategiasAtivasRedis();

}
