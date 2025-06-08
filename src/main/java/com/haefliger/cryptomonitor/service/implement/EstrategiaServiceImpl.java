package com.haefliger.cryptomonitor.service.implement;

import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.EstrategiaResponse;
import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.mapper.EstrategiaMapper;
import com.haefliger.cryptomonitor.repository.EstrategiaRepository;
import com.haefliger.cryptomonitor.service.EstrategiaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class EstrategiaServiceImpl implements EstrategiaService {

    private final EstrategiaRepository repository;
    private final EstrategiaMapper mapper;

    @Override
    public EstrategiaResponse salvarEstrategia(EstrategiaRequest estrategiaRequest) {
        log.info("Salvando estratégia: {}", estrategiaRequest);

        final Estrategia estrategia = mapper.requestToEntityEstrategia(estrategiaRequest);
        final List<CondicaoEstrategia> condicoes = mapper.requestToEntityCondicaoEstrategia(estrategiaRequest.getCondicoes());

        condicoes.forEach(cond -> cond.setEstrategia(estrategia));
        estrategia.setCondicoes(condicoes);

        final Estrategia savedEstrategia = repository.save(estrategia);
        return mapper.longToResponseEstrategia(savedEstrategia.getId());
    }

}
