package com.haefliger.cryptomonitor.service.implement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haefliger.cryptomonitor.dto.cache.EstrategiaCacheDTO;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.mapper.EstrategiaCacheMapper;
import com.haefliger.cryptomonitor.properties.RedisKeysProperties;
import com.haefliger.cryptomonitor.repository.EstrategiaRepository;
import com.haefliger.cryptomonitor.service.RedisService;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RedisServiceImpl implements RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisServiceImpl.class);

    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;
    private final RedisKeysProperties redisKeys;
    private final EstrategiaRepository repository;
    private final EstrategiaCacheMapper estrategiaCacheMapper;
    private final ObjectMapper objectMapper;

    RedisServiceImpl(
            RedisDataSource redisDataSource,
            RedisKeysProperties redisKeys,
            EstrategiaRepository repository,
            EstrategiaCacheMapper estrategiaCacheMapper,
            ObjectMapper objectMapper) {
        this.valueCommands = redisDataSource.value(String.class, String.class);
        this.keyCommands = redisDataSource.key(String.class);
        this.redisKeys = redisKeys;
        this.repository = repository;
        this.estrategiaCacheMapper = estrategiaCacheMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void salvarEstrategiasAtivasRedis(List<EstrategiaCacheDTO> estrategias) {
        try {
            valueCommands.set(
                    redisKeys.estrategiasAtivas(), objectMapper.writeValueAsString(estrategias));
            log.info("Estrategias ativas salvas no Redis: {}", estrategias.size());
        } catch (Exception e) {
            log.error("Erro ao salvar estratégias ativas no Redis: {}", e.getMessage(), e);
        }
    }

    // Chamado de thread de executor (EstrategiaAsyncService), fora de request e fora de
    // transacao. Sem isto o EntityManager injetado nao tem sessao propria por chamada e
    // duas atualizacoes simultaneas se atropelam ("ResultSet esta fechado"), deixando o
    // cache apagado sem nunca ser reescrito.
    @Override
    @Transactional
    public List<Estrategia> buscarEstrategiasAtivasRedis() {
        List<EstrategiaCacheDTO> cacheDTOs = lerCache();
        if (cacheDTOs != null && !cacheDTOs.isEmpty()) {
            return estrategiaCacheMapper.cacheToEntity(cacheDTOs);
        }

        List<Estrategia> estrategias = repository.findByAtivoFetchCondicoes(true);
        if (estrategias != null && !estrategias.isEmpty()) {
            salvarEstrategiasAtivasRedis(estrategiaCacheMapper.toCacheDTOList(estrategias));
            return estrategias;
        }

        log.warn("Nenhuma estratégia ativa encontrada no Redis ou no banco de dados.");
        return List.of();
    }

    private List<EstrategiaCacheDTO> lerCache() {
        String cru = valueCommands.get(redisKeys.estrategiasAtivas());
        if (cru == null || cru.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(cru, new TypeReference<List<EstrategiaCacheDTO>>() {});
        } catch (Exception e) {
            log.error(
                    "Cache de estratégias ativas ilegível, caindo para o banco: {}",
                    e.getMessage());
            return List.of();
        }
    }

    @Override
    public void excluirEstrategiasAtivasRedis() {
        keyCommands.del(redisKeys.estrategiasAtivas());
        log.info("Estrategias ativas removidas do Redis");
    }
}
