package com.haefliger.cryptomonitor.service.implement;

import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.SalvarEstrategiaResponse;
import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.mapper.EstrategiaMapper;
import com.haefliger.cryptomonitor.repository.EstrategiaRepository;
import com.haefliger.cryptomonitor.service.EstrategiaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class EstrategiaServiceImpl implements EstrategiaService {

    private final EstrategiaRepository repository;
    private final EstrategiaMapper mapper;

    @Override
    @Transactional
    public SalvarEstrategiaResponse salvarEstrategia(EstrategiaRequest estrategiaRequest) {
        try {
            log.info("Salvando estratégia");
            final Estrategia estrategia = mapRequestToEstrategia(estrategiaRequest);
            final Estrategia savedEstrategia = repository.save(estrategia);
            return mapper.longToSalvarEstrategiaResponse(savedEstrategia.getId());
        } catch (RuntimeException e) {
            log.error("Erro ao salvar estratégia: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar estratégia", e);
        }
    }

    private Estrategia mapRequestToEstrategia(EstrategiaRequest estrategiaRequest) {
        final Estrategia estrategia = mapper.requestToEntityEstrategia(estrategiaRequest);
        final List<CondicaoEstrategia> condicoes = mapper.requestToEntityCondicaoEstrategia(estrategiaRequest.getCondicoes());
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
        } catch (RuntimeException e) {
            log.error("Erro ao buscar estratégias: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar estratégias", e);
        }
    }
}