package com.haefliger.cryptomonitor.service.implement;

import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.SalvarEstrategiaResponse;
import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.mapper.EstrategiaMapper;
import com.haefliger.cryptomonitor.repository.EstrategiaRepository;
import com.haefliger.cryptomonitor.service.EstrategiaService;
import com.haefliger.cryptomonitor.service.RedisService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class EstrategiaServiceImpl implements EstrategiaService {

    private static final Logger log = LoggerFactory.getLogger(EstrategiaServiceImpl.class);

    private final EstrategiaRepository repository;
    private final EstrategiaMapper mapper;
    private final RedisService redisService;
    private final EstrategiaAsyncService estrategiaAsyncService;
    private final TransactionSynchronizationRegistry transactionRegistry;

    EstrategiaServiceImpl(EstrategiaRepository repository,
                          EstrategiaMapper mapper,
                          RedisService redisService,
                          EstrategiaAsyncService estrategiaAsyncService,
                          TransactionSynchronizationRegistry transactionRegistry) {
        this.repository = repository;
        this.mapper = mapper;
        this.redisService = redisService;
        this.estrategiaAsyncService = estrategiaAsyncService;
        this.transactionRegistry = transactionRegistry;
    }

    @Override
    @Transactional
    public SalvarEstrategiaResponse salvarEstrategia(EstrategiaRequest estrategiaRequest) {
        try {
            log.info("Salvando estratégia: {}", estrategiaRequest.nome());
            final Estrategia estrategia = toEstrategiaEntity(estrategiaRequest);
            final Estrategia savedEstrategia = repository.save(estrategia);

            atualizarWebSocket();
            return mapper.longToSalvarEstrategiaResponse(savedEstrategia.getId());
        } catch (Exception e) {
            log.error("Erro ao salvar estratégia: {}", estrategiaRequest.nome(), e);
            throw new ServiceException("Erro ao salvar estratégia", e);
        }
    }

    private Estrategia toEstrategiaEntity(EstrategiaRequest estrategiaRequest) {
        Estrategia estrategia = mapper.requestToEntityEstrategia(estrategiaRequest, true);
        List<CondicaoEstrategia> condicoes = mapper.requestToEntityCondicaoEstrategia(estrategiaRequest.condicoes());
        condicoes.forEach(cond -> cond.setEstrategia(estrategia));
        estrategia.setCondicoes(condicoes);
        return estrategia;
    }

    @Override
    public BuscarEstrategiaResponse buscarEstrategia(Boolean ativo) {
        try {
            log.info("Buscando estratégias com ativo: {}", ativo);
            final List<Estrategia> estrategias = (ativo != null) ? repository.findByAtivo(ativo) : repository.findAll();
            return mapper.entityListToBuscarEstrategiaResponse(estrategias);
        } catch (Exception e) {
            log.error("Erro ao buscar estratégias: {}", e.getMessage(), e);
            throw new ServiceException("Erro ao buscar estratégias", e);
        }
    }

    @Override
    @Transactional
    public void deletarEstrategia(Long id) {
        try {
            log.info("Deletando estratégia com id: {}", id);
            repository.deleteById(id);
            atualizarWebSocket();
        } catch (Exception e) {
            log.error("Erro ao deletar estratégia com id {}: {}", id, e.getMessage(), e);
            throw new ServiceException("Erro ao deletar estratégia", e);
        }
    }

    @Override
    @Transactional
    public void statusEstrategia(Long id, Boolean ativo, Boolean permanente) {
        try {
            log.info("Alterando status da estratégia com id: {} para ativo: {}", id, ativo);
            Estrategia estrategia = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Estratégia não encontrada"));
            mapUpdateEstrategia(estrategia, ativo, permanente);
            repository.save(estrategia);
            atualizarWebSocket();
        } catch (Exception e) {
            log.error("Erro ao alterar status estratégia com id {}: {}", id, e.getMessage(), e);
            throw new ServiceException("Erro ao alterar status da estratégia", e);
        }
    }

    private void mapUpdateEstrategia(Estrategia estrategia, Boolean ativo, Boolean permanente) {
        estrategia.setAtivo(ativo);
        estrategia.setPermanente(permanente);
        estrategia.setDateLastUpdate(LocalDateTime.now());
    }

    private void atualizarWebSocket() {
        transactionRegistry.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
                // nada a fazer antes do commit
            }

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    redisService.excluirEstrategiasAtivasRedis();
                    estrategiaAsyncService.atualizaEstrategiasWS();
                }
            }
        });
    }
}
