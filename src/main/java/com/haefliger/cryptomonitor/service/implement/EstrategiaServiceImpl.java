package com.haefliger.cryptomonitor.service.implement;

import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.SalvarEstrategiaResponse;
import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.mapper.EstrategiaMapper;
import com.haefliger.cryptomonitor.repository.EstrategiaRepository;
import com.haefliger.cryptomonitor.service.EstrategiaService;
import com.haefliger.cryptomonitor.service.KafkaService;
import com.haefliger.cryptomonitor.service.RedisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class EstrategiaServiceImpl implements EstrategiaService {

    private final EstrategiaRepository repository;
    private final EstrategiaMapper mapper;
    private final KafkaService kafkaService;
    private final RedisService redisService;
    private final EstrategiaAsyncService estrategiaAsyncService;

    @Override
    @Transactional
    public SalvarEstrategiaResponse salvarEstrategia(EstrategiaRequest estrategiaRequest) {
        try {
            log.info("Salvando estratégia: {}", estrategiaRequest.getNome());
            final Estrategia estrategia = toEstrategiaEntity(estrategiaRequest);
            final Estrategia savedEstrategia = repository.save(estrategia);
//            kafkaService.sendMessage(ESTRATEGIA_INSERT.getTopic(), estrategia.getId().toString(), estrategia);

            atualizarWebSocket();

            return mapper.longToSalvarEstrategiaResponse(savedEstrategia.getId());
        } catch (Exception e) {
            log.error("Erro ao salvar estratégia: {}", estrategiaRequest.getNome(), e);
            throw new ServiceException("Erro ao salvar estratégia", e);
        }
    }

    private Estrategia toEstrategiaEntity(EstrategiaRequest estrategiaRequest) {
        Estrategia estrategia = mapper.requestToEntityEstrategia(estrategiaRequest, true);
        List<CondicaoEstrategia> condicoes = mapper.requestToEntityCondicaoEstrategia(estrategiaRequest.getCondicoes());
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
        // Garante que a atualização via WebSocket só ocorra após o commit da transação
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisService.excluirEstrategiasAtivasRedis();
                estrategiaAsyncService.atualizaEstrategiasWS();
            }
        });
    }
}