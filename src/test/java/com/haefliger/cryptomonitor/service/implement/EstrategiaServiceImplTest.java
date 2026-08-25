package com.haefliger.cryptomonitor.service.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.haefliger.cryptomonitor.dto.request.estrategia.CondicaoRequest;
import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.dto.response.estrategia.BuscarEstrategiaResponse;
import com.haefliger.cryptomonitor.dto.response.estrategia.SalvarEstrategiaResponse;
import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.OperadorLogicoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.mapper.EstrategiaMapper;
import com.haefliger.cryptomonitor.repository.EstrategiaRepository;
import com.haefliger.cryptomonitor.service.RedisService;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstrategiaServiceImpl — caracterização do comportamento atual")
class EstrategiaServiceImplTest {

    @Mock private EstrategiaRepository repository;
    @Mock private EstrategiaMapper mapper;
    @Mock private RedisService redisService;
    @Mock private EstrategiaAsyncService estrategiaAsyncService;
    @Mock private TransactionSynchronizationRegistry transactionRegistry;

    @InjectMocks private EstrategiaServiceImpl service;

    private static EstrategiaRequest request() {
        CondicaoRequest condicao =
                new CondicaoRequest(TipoIndicadorEnum.RSI, OperadorComparacaoEnum.MENOR, 30);
        return new EstrategiaRequest(
                "Bitcoin RSI",
                "BTCUSDT",
                "60",
                OperadorLogicoEnum.AND,
                Boolean.FALSE,
                List.of(condicao));
    }

    private static Estrategia entidade(Long id) {
        Estrategia estrategia = new Estrategia();
        estrategia.setId(id);
        estrategia.setNome("Bitcoin RSI");
        estrategia.setSimbolo("BTCUSDT");
        estrategia.setIntervalo("60");
        estrategia.setOperadorLogico(OperadorLogicoEnum.AND);
        estrategia.setAtivo(Boolean.TRUE);
        estrategia.setPermanente(Boolean.FALSE);
        return estrategia;
    }

    private Synchronization sincronizacaoRegistrada() {
        ArgumentCaptor<Synchronization> captor = ArgumentCaptor.forClass(Synchronization.class);
        verify(transactionRegistry).registerInterposedSynchronization(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName(
            "salvar mapeia a request com ativo=true, liga cada condição à estratégia e devolve o id")
    void salvarEstrategia() {
        Estrategia mapeada = entidade(null);
        CondicaoEstrategia condicao = new CondicaoEstrategia();
        condicao.setTipoIndicador(TipoIndicadorEnum.RSI);
        condicao.setOperador(OperadorComparacaoEnum.MENOR);
        condicao.setValor(BigDecimal.valueOf(30));

        when(mapper.requestToEntityEstrategia(any(), eq(Boolean.TRUE))).thenReturn(mapeada);
        when(mapper.requestToEntityCondicaoEstrategia(anyList())).thenReturn(List.of(condicao));
        when(repository.save(mapeada)).thenReturn(entidade(7L));
        when(mapper.longToSalvarEstrategiaResponse(7L))
                .thenReturn(new SalvarEstrategiaResponse(7L));

        SalvarEstrategiaResponse resposta = service.salvarEstrategia(request());

        assertThat(resposta.id()).isEqualTo(7L);
        assertThat(condicao.getEstrategia()).isSameAs(mapeada);
        assertThat(mapeada.getCondicoes()).containsExactly(condicao);
    }

    @Test
    @DisplayName("qualquer falha ao salvar vira ServiceException com a causa original preservada")
    void salvarEncapsulaFalha() {
        when(mapper.requestToEntityEstrategia(any(), eq(Boolean.TRUE)))
                .thenThrow(new IllegalStateException("mapper quebrou"));

        assertThatThrownBy(() -> service.salvarEstrategia(request()))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Erro ao salvar estratégia")
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("buscar sem filtro usa findAll; com filtro usa findByAtivo")
    void buscarEstrategia() {
        BuscarEstrategiaResponse resposta = new BuscarEstrategiaResponse(List.of());
        when(mapper.entityListToBuscarEstrategiaResponse(anyList())).thenReturn(resposta);
        when(repository.findAll()).thenReturn(List.of(entidade(1L)));
        when(repository.findByAtivo(Boolean.TRUE)).thenReturn(List.of(entidade(2L)));

        assertThat(service.buscarEstrategia(null)).isSameAs(resposta);
        assertThat(service.buscarEstrategia(Boolean.TRUE)).isSameAs(resposta);

        verify(repository).findAll();
        verify(repository).findByAtivo(Boolean.TRUE);
    }

    @Test
    void buscarEncapsulaFalha() {
        when(repository.findAll()).thenThrow(new IllegalStateException("banco fora"));

        assertThatThrownBy(() -> service.buscarEstrategia(null))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Erro ao buscar estratégias");
    }

    @Test
    void deletarEstrategia() {
        service.deletarEstrategia(9L);

        verify(repository).deleteById(9L);
    }

    @Test
    @DisplayName("alterar status grava ativo, permanente e carimba dateLastUpdate")
    void statusEstrategia() {
        when(repository.findById(3L)).thenReturn(Optional.of(entidade(3L)));

        LocalDateTime antes = LocalDateTime.now();
        service.statusEstrategia(3L, Boolean.FALSE, Boolean.TRUE);

        ArgumentCaptor<Estrategia> captor = ArgumentCaptor.forClass(Estrategia.class);
        verify(repository).save(captor.capture());
        Estrategia salva = captor.getValue();
        assertThat(salva.getAtivo()).isFalse();
        assertThat(salva.getPermanente()).isTrue();
        assertThat(salva.getDateLastUpdate()).isNotNull().isAfterOrEqualTo(antes);
    }

    @Test
    @DisplayName("estratégia inexistente também sai como ServiceException, não como 404")
    void statusDeEstrategiaInexistente() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.statusEstrategia(404L, Boolean.TRUE, Boolean.FALSE))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Erro ao alterar status da estratégia")
                .hasRootCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Redis e WebSocket só são tocados depois do commit, nunca durante a operação")
    void efeitosColateraisSoDepoisDoCommit() {
        when(repository.findById(3L)).thenReturn(Optional.of(entidade(3L)));

        service.statusEstrategia(3L, Boolean.FALSE, Boolean.FALSE);

        verifyNoInteractions(redisService);
        verify(estrategiaAsyncService, never()).atualizaEstrategiasWS();

        sincronizacaoRegistrada().afterCompletion(Status.STATUS_COMMITTED);

        verify(redisService).excluirEstrategiasAtivasRedis();
        verify(estrategiaAsyncService).atualizaEstrategiasWS();
    }

    @Test
    @DisplayName("rollback não invalida cache nem reconecta o WebSocket")
    void rollbackNaoDisparaEfeitos() {
        when(repository.findById(3L)).thenReturn(Optional.of(entidade(3L)));

        service.statusEstrategia(3L, Boolean.FALSE, Boolean.FALSE);
        sincronizacaoRegistrada().afterCompletion(Status.STATUS_ROLLEDBACK);

        verifyNoInteractions(redisService);
        verify(estrategiaAsyncService, never()).atualizaEstrategiasWS();
    }

    @Test
    @DisplayName(
            "sem transação ativa, registrar a sincronização estoura e a operação inteira falha")
    void semTransacaoAtivaFalha() {
        when(repository.findById(3L)).thenReturn(Optional.of(entidade(3L)));
        doThrow(new IllegalStateException("Transaction is not active"))
                .when(transactionRegistry)
                .registerInterposedSynchronization(any());

        assertThatThrownBy(() -> service.statusEstrategia(3L, Boolean.TRUE, Boolean.FALSE))
                .isInstanceOf(ServiceException.class)
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }
}
