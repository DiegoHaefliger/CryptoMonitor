package com.haefliger.cryptomonitor.service.implement;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.haefliger.cryptomonitor.dto.cache.EstrategiaCacheDTO;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.mapper.EstrategiaCacheMapper;
import com.haefliger.cryptomonitor.properties.ParametersProperties;
import com.haefliger.cryptomonitor.repository.EstrategiaRepository;
import com.haefliger.cryptomonitor.service.RedisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author diego-haefliger
 * Date 6/17/25
 */

@AllArgsConstructor
@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> estrategiaRedisTemplate;
    private final ParametersProperties parametersProperties;
    private final EstrategiaRepository repository;
    private final EstrategiaCacheMapper estrategiaCacheMapper;
    private final ObjectMapper objectMapper;


    @Override
    public void salvarEstrategiasAtivasRedis(List<EstrategiaCacheDTO> estrategias) {
        estrategiaRedisTemplate.opsForValue().set(parametersProperties.getRedisEstrategiasAtivasKey(), estrategias);
        log.info("Estrategias ativas salvas no Redis: {}", estrategias.size());
    }

    @Override
    public List<Estrategia> buscarEstrategiasAtivasRedis() {
        Object rawCached = estrategiaRedisTemplate.opsForValue().get(parametersProperties.getRedisEstrategiasAtivasKey());
        if (rawCached instanceof List<?> cached && !cached.isEmpty() && cached.get(0) instanceof java.util.LinkedHashMap) {
            List<EstrategiaCacheDTO> cacheDTOs = cached.stream()
                .map(obj -> objectMapper.convertValue(obj, EstrategiaCacheDTO.class))
                .toList();
            return estrategiaCacheMapper.cacheToEntity(cacheDTOs);
        }

        // Busca do banco caso não esteja no cache
        List<Estrategia> estrategias = repository.findByAtivoFetchCondicoes(true);
        if (estrategias != null && !estrategias.isEmpty()) {
            List<EstrategiaCacheDTO> cacheDTOs = estrategiaCacheMapper.toCacheDTOList(estrategias);
            salvarEstrategiasAtivasRedis(cacheDTOs);
            return estrategias;
        }

        log.warn("Nenhuma estratégia ativa encontrada no Redis ou no banco de dados.");
        return List.of();
    }

    @Override
    public void excluirEstrategiasAtivasRedis() {
        estrategiaRedisTemplate.delete(parametersProperties.getRedisEstrategiasAtivasKey());
        log.info("Estrategias ativas removidas do Redis");
    }
}
