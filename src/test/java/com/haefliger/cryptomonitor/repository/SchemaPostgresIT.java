package com.haefliger.cryptomonitor.repository;

import com.haefliger.cryptomonitor.entity.CondicaoEstrategia;
import com.haefliger.cryptomonitor.entity.Estrategia;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.OperadorLogicoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.liquibase.change-log=classpath:db/changelog/changelog-master.xml",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.datasource.url=${DATABASE_JDBC_URL:jdbc:postgresql://localhost:5432/crypto_monitor}",
        "spring.datasource.username=${DB_USER:admin}",
        "spring.datasource.password=${DB_PASSWORD:}"
})
@EnabledIf("postgresDisponivel")
@DisplayName("Schema no Postgres — Liquibase e mapeamento das entidades")
class SchemaPostgresIT {

    private static final String URL =
            System.getenv().getOrDefault("DATABASE_JDBC_URL", "jdbc:postgresql://localhost:5432/crypto_monitor");
    private static final String USUARIO = System.getenv().getOrDefault("DB_USER", "admin");
    private static final String SENHA = System.getenv().getOrDefault("DB_PASSWORD", "");

    static boolean postgresDisponivel() {
        try (Connection ignored = DriverManager.getConnection(URL, USUARIO, SENHA)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Autowired
    private EntityManager em;

    @Autowired
    private EstrategiaRepository estrategiaRepository;

    private static Estrategia comCondicao() {
        Estrategia estrategia = Estrategia.builder()
                .nome("Bitcoin RSI")
                .simbolo("BTCUSDT")
                .intervalo("60")
                .operadorLogico(OperadorLogicoEnum.AND)
                .ativo(Boolean.TRUE)
                .permanente(Boolean.FALSE)
                .build();
        CondicaoEstrategia condicao = CondicaoEstrategia.builder()
                .tipoIndicador(TipoIndicadorEnum.RSI)
                .operador(OperadorComparacaoEnum.MENOR_IGUAL)
                .valor(new BigDecimal("30.12345678"))
                .estrategia(estrategia)
                .build();
        estrategia.setCondicoes(List.of(condicao));
        return estrategia;
    }

    @Test
    @DisplayName("Liquibase cria as duas tabelas e o Hibernate valida o mapeamento contra elas")
    void schemaCriadoEValidado() {
        @SuppressWarnings("unchecked")
        List<String> tabelas = em.createNativeQuery(
                        "select table_name from information_schema.tables where table_schema = 'public' order by table_name")
                .getResultList();

        assertThat(tabelas).contains("estrategias", "condicoes_estrategia", "databasechangelog");
    }

    @Test
    @DisplayName("colunas booleanas nascem BOOLEAN nativo, não TINYINT como era no MySQL")
    void colunasBooleanas() {
        @SuppressWarnings("unchecked")
        List<Object[]> colunas = em.createNativeQuery(
                        "select column_name, data_type from information_schema.columns"
                                + " where table_name = 'estrategias' and column_name in ('ativo', 'permanente')")
                .getResultList();

        assertThat(colunas).hasSize(2)
                .allSatisfy(coluna -> assertThat(coluna[1]).isEqualTo("boolean"));
    }

    @Test
    @DisplayName("id é identity do Postgres e vem preenchido depois do insert")
    void identityGeraId() {
        Estrategia salva = estrategiaRepository.save(comCondicao());
        em.flush();

        assertThat(salva.getId()).isNotNull().isPositive();
        assertThat(salva.getCondicoes().get(0).getId()).isNotNull();
    }

    @Test
    @DisplayName("enum grava como texto do name(), não como ordinal")
    void enumComoTexto() {
        Estrategia salva = estrategiaRepository.save(comCondicao());
        em.flush();
        em.clear();

        Object[] linha = (Object[]) em.createNativeQuery(
                        "select e.operador_logico, c.tipo_indicador, c.operador"
                                + " from estrategias e join condicoes_estrategia c on c.estrategia_id = e.id"
                                + " where e.id = :id")
                .setParameter("id", salva.getId())
                .getSingleResult();

        assertThat(linha).containsExactly("AND", "RSI", "MENOR_IGUAL");
    }

    @Test
    @DisplayName("BigDecimal mantém as 8 casas decimais declaradas na coluna")
    void precisaoDoValor() {
        Estrategia salva = estrategiaRepository.save(comCondicao());
        em.flush();
        em.clear();

        CondicaoEstrategia lida = em.find(CondicaoEstrategia.class, salva.getCondicoes().get(0).getId());

        assertThat(lida.getValor()).isEqualByComparingTo("30.12345678");
        assertThat(lida.getValor().scale()).isEqualTo(8);
    }

    @Test
    @DisplayName("date_created ganha CURRENT_TIMESTAMP e o @PrePersist preenche antes do insert")
    void dataDeCriacao() {
        Estrategia salva = estrategiaRepository.save(comCondicao());
        em.flush();

        assertThat(salva.getDateCreated()).isNotNull();
        assertThat(salva.getCondicoes().get(0).getDateCreated()).isNotNull();
    }

    @Test
    @DisplayName("o CHECK de operador_logico continua valendo no Postgres")
    void checkDeOperadorLogico() {
        assertThatThrownBy(() -> {
            em.createNativeQuery(
                            "insert into estrategias (nome, simbolo, intervalo, operador_logico, ativo, permanente, date_created)"
                                    + " values ('x', 'BTCUSDT', '60', 'XOR', true, false, now())")
                    .executeUpdate();
            em.flush();
        }).hasMessageContaining("chk_operador_logico");
    }

    @Test
    @DisplayName("apagar a estratégia leva as condições junto pela FK ON DELETE CASCADE do banco")
    void cascataNoBanco() {
        Estrategia salva = estrategiaRepository.save(comCondicao());
        em.flush();
        em.clear();

        em.createNativeQuery("delete from estrategias where id = :id")
                .setParameter("id", salva.getId())
                .executeUpdate();

        Number restantes = (Number) em.createNativeQuery(
                        "select count(*) from condicoes_estrategia where estrategia_id = :id")
                .setParameter("id", salva.getId())
                .getSingleResult();

        assertThat(restantes.intValue()).isZero();
    }

    @Test
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
