package com.haefliger.cryptomonitor.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.OperadorLogicoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@DisplayName("Schema no Postgres — Liquibase e mapeamento das entidades")
class SchemaPostgresTest {

    @Inject EntityManager em;

    @Inject EstrategiaRepository estrategiaRepository;

    private static Estrategia comCondicao() {
        Estrategia estrategia = new Estrategia();
        estrategia.setNome("Bitcoin RSI");
        estrategia.setSimbolo("BTCUSDT");
        estrategia.setIntervalo("60");
        estrategia.setOperadorLogico(OperadorLogicoEnum.AND);
        estrategia.setAtivo(Boolean.TRUE);
        estrategia.setPermanente(Boolean.FALSE);

        CondicaoEstrategia condicao = new CondicaoEstrategia();
        condicao.setTipoIndicador(TipoIndicadorEnum.RSI);
        condicao.setOperador(OperadorComparacaoEnum.MENOR_IGUAL);
        condicao.setValor(new BigDecimal("30.12345678"));
        condicao.setEstrategia(estrategia);

        estrategia.setCondicoes(List.of(condicao));
        return estrategia;
    }

    @Test
    @TestTransaction
    @DisplayName("Liquibase cria as duas tabelas e o Hibernate valida o mapeamento contra elas")
    void schemaCriadoEValidado() {
        @SuppressWarnings("unchecked")
        List<String> tabelas =
                em.createNativeQuery(
                                "select table_name from information_schema.tables where table_schema = 'public'")
                        .getResultList();

        assertThat(tabelas).contains("estrategias", "condicoes_estrategia", "databasechangelog");
    }

    @Test
    @TestTransaction
    @DisplayName("colunas booleanas nascem BOOLEAN nativo, não TINYINT como era no MySQL")
    void colunasBooleanas() {
        @SuppressWarnings("unchecked")
        List<Object[]> colunas =
                em.createNativeQuery(
                                "select column_name, data_type from information_schema.columns"
                                        + " where table_name = 'estrategias' and column_name in ('ativo', 'permanente')")
                        .getResultList();

        assertThat(colunas)
                .hasSize(2)
                .allSatisfy(coluna -> assertThat(coluna[1]).isEqualTo("boolean"));
    }

    @Test
    @TestTransaction
    @DisplayName("valor é numeric(20,8), não o text que o changelog criava antes da F2")
    void tipoDaColunaValor() {
        Object[] coluna =
                (Object[])
                        em.createNativeQuery(
                                        "select data_type, numeric_precision, numeric_scale from information_schema.columns"
                                                + " where table_name = 'condicoes_estrategia' and column_name = 'valor'")
                                .getSingleResult();

        assertThat(coluna[0]).isEqualTo("numeric");
        assertThat(((Number) coluna[1]).intValue()).isEqualTo(20);
        assertThat(((Number) coluna[2]).intValue()).isEqualTo(8);
    }

    @Test
    @TestTransaction
    @DisplayName("id é identity do Postgres e vem preenchido depois do insert")
    void identityGeraId() {
        Estrategia salva = estrategiaRepository.save(comCondicao());
        em.flush();

        assertThat(salva.getId()).isNotNull().isPositive();
        assertThat(salva.getCondicoes().get(0).getId()).isNotNull();
    }

    @Test
    @TestTransaction
    @DisplayName("enum grava como texto do name(), não como ordinal")
    void enumComoTexto() {
        Estrategia salva = estrategiaRepository.save(comCondicao());
        em.flush();
        em.clear();

        Object[] linha =
                (Object[])
                        em.createNativeQuery(
                                        "select e.operador_logico, c.tipo_indicador, c.operador"
                                                + " from estrategias e join condicoes_estrategia c on c.estrategia_id = e.id"
                                                + " where e.id = :id")
                                .setParameter("id", salva.getId())
                                .getSingleResult();

        assertThat(linha).containsExactly("AND", "RSI", "MENOR_IGUAL");
    }

    @Test
    @TestTransaction
    @DisplayName("BigDecimal mantém as 8 casas decimais declaradas na coluna")
    void precisaoDoValor() {
        Estrategia salva = estrategiaRepository.save(comCondicao());
        em.flush();
        em.clear();

        CondicaoEstrategia lida =
                em.find(CondicaoEstrategia.class, salva.getCondicoes().get(0).getId());

        assertThat(lida.getValor()).isEqualByComparingTo("30.12345678");
        assertThat(lida.getValor().scale()).isEqualTo(8);
    }

    @Test
    @TestTransaction
    @DisplayName("date_created é preenchido pelo @PrePersist antes do insert")
    void dataDeCriacao() {
        Estrategia salva = estrategiaRepository.save(comCondicao());
        em.flush();

        assertThat(salva.getDateCreated()).isNotNull();
        assertThat(salva.getCondicoes().get(0).getDateCreated()).isNotNull();
    }

    @Test
    @TestTransaction
    @DisplayName("o CHECK de operador_logico continua valendo no Postgres")
    void checkDeOperadorLogico() {
        assertThatThrownBy(
                        () -> {
                            em.createNativeQuery(
                                            "insert into estrategias (nome, simbolo, intervalo, operador_logico, ativo, permanente, date_created)"
                                                    + " values ('x', 'BTCUSDT', '60', 'XOR', true, false, now())")
                                    .executeUpdate();
                            em.flush();
                        })
                .hasMessageContaining("chk_operador_logico");
    }

    @Test
    @TestTransaction
    @DisplayName("apagar a estratégia leva as condições junto pela FK ON DELETE CASCADE do banco")
    void cascataNoBanco() {
        Estrategia salva = estrategiaRepository.save(comCondicao());
        em.flush();
        em.clear();

        em.createNativeQuery("delete from estrategias where id = :id")
                .setParameter("id", salva.getId())
                .executeUpdate();

        Number restantes =
                (Number)
                        em.createNativeQuery(
                                        "select count(*) from condicoes_estrategia where estrategia_id = :id")
                                .setParameter("id", salva.getId())
                                .getSingleResult();

        assertThat(restantes.intValue()).isZero();
    }

    @Test
    @TestTransaction
    @DisplayName("consulta com join fetch das condições funciona no Postgres")
    void buscaComJoinFetch() {
        estrategiaRepository.save(comCondicao());
        em.flush();
        em.clear();

        List<Estrategia> ativas = estrategiaRepository.findByAtivoFetchCondicoes(true);

        assertThat(ativas).hasSize(1);
        assertThat(ativas.get(0).getCondicoes()).hasSize(1);
    }
}
