package com.haefliger.cryptomonitor.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.haefliger.cryptomonitor.dto.request.estrategia.CondicaoRequest;
import com.haefliger.cryptomonitor.dto.request.estrategia.EstrategiaRequest;
import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.enums.OperadorLogicoEnum;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Bean Validation da EstrategiaRequest — caracterização das mensagens atuais")
class EstrategiaRequestValidacaoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void abre() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecha() {
        factory.close();
    }

    private static CondicaoRequest condicaoValida() {
        return new CondicaoRequest(TipoIndicadorEnum.RSI, OperadorComparacaoEnum.MENOR, 30);
    }

    private record Cenario(
            String nome,
            String simbolo,
            String intervalo,
            OperadorLogicoEnum operadorLogico,
            Boolean permanente,
            List<CondicaoRequest> condicoes) {

        EstrategiaRequest build() {
            return new EstrategiaRequest(
                    nome, simbolo, intervalo, operadorLogico, permanente, condicoes);
        }

        Cenario nome(String valor) {
            return new Cenario(valor, simbolo, intervalo, operadorLogico, permanente, condicoes);
        }

        Cenario simbolo(String valor) {
            return new Cenario(nome, valor, intervalo, operadorLogico, permanente, condicoes);
        }

        Cenario intervalo(String valor) {
            return new Cenario(nome, simbolo, valor, operadorLogico, permanente, condicoes);
        }

        Cenario operadorLogico(OperadorLogicoEnum valor) {
            return new Cenario(nome, simbolo, intervalo, valor, permanente, condicoes);
        }

        Cenario permanente(Boolean valor) {
            return new Cenario(nome, simbolo, intervalo, operadorLogico, valor, condicoes);
        }

        Cenario condicoes(List<CondicaoRequest> valor) {
            return new Cenario(nome, simbolo, intervalo, operadorLogico, permanente, valor);
        }
    }

    private static Cenario valida() {
        return new Cenario(
                "Bitcoin RSI",
                "BTCUSDT",
                "60",
                OperadorLogicoEnum.AND,
                Boolean.FALSE,
                List.of(condicaoValida()));
    }

    private static List<String> mensagens(EstrategiaRequest request) {
        Set<ConstraintViolation<EstrategiaRequest>> violacoes = validator.validate(request);
        return violacoes.stream().map(ConstraintViolation::getMessage).sorted().toList();
    }

    @Test
    void requestCompletaNaoTemViolacao() {
        assertThat(mensagens(valida().build())).isEmpty();
    }

    @Test
    void nomeVazio() {
        assertThat(mensagens(valida().nome("   ").build()))
                .containsExactly("Campo 'nome' não pode ser vazio");
        assertThat(mensagens(valida().nome(null).build()))
                .containsExactly("Campo 'nome' não pode ser vazio");
    }

    @ParameterizedTest
    @ValueSource(strings = {"BTC-USDT", "BTC USDT", ""})
    void simboloComHifenEspacoOuVazio(String simbolo) {
        assertThat(mensagens(valida().simbolo(simbolo).build()))
                .containsExactly(
                        "Símbolo inválido: não pode ser vazio, não pode conter espaços ou hífens");
    }

    @Test
    void intervaloForaDaLista() {
        assertThat(mensagens(valida().intervalo("7").build()))
                .containsExactly(
                        "Intervalo inválido. Valores válidos: 1, 5, 15, 30, 60, 120, 180, 240, 360, 720, D, W, M");
    }

    @Test
    void operadorLogicoNulo() {
        assertThat(mensagens(valida().operadorLogico(null).build()))
                .containsExactly("Operador lógico inválido. Valores válidos: AND, OR");
    }

    @Test
    void permanenteNulo() {
        assertThat(mensagens(valida().permanente(null).build()))
                .containsExactly("Campo 'permanente' não pode ser vazio");
    }

    @Test
    @DisplayName(
            "lista vazia dispara só o @NotEmpty padrão — o @NotEmptyWithFieldMessage não enxerga coleção vazia")
    void condicoesVazia() {
        assertThat(mensagens(valida().condicoes(List.of()).build()))
                .containsExactly("não deve estar vazio");
    }

    @Test
    @DisplayName("lista nula dispara as duas violações, aí sim com a mensagem customizada")
    void condicoesNula() {
        assertThat(mensagens(valida().condicoes(null).build()))
                .containsExactly("Campo 'conditions' não pode ser vazio", "não deve estar vazio");
    }

    @Test
    void tipoIndicadorNuloNaCondicaoAninhada() {
        CondicaoRequest condicao = new CondicaoRequest(null, OperadorComparacaoEnum.MENOR, 30);

        assertThat(mensagens(valida().condicoes(List.of(condicao)).build()))
                .containsExactly(
                        "Tipo de indicador inválido. Valores válidos: PRECO, RSI, MEDIA_MOVEL");
    }

    @Test
    void operadorNuloNaCondicaoAninhada() {
        CondicaoRequest condicao = new CondicaoRequest(TipoIndicadorEnum.RSI, null, 30);

        assertThat(mensagens(valida().condicoes(List.of(condicao)).build()))
                .containsExactly("Operador inválido. Valores válidos: <, >, <=, >=, =");
    }

    @Test
    void valorNuloNaCondicaoAninhada() {
        CondicaoRequest condicao =
                new CondicaoRequest(TipoIndicadorEnum.RSI, OperadorComparacaoEnum.MENOR, null);

        assertThat(mensagens(valida().condicoes(List.of(condicao)).build()))
                .containsExactly("Campo 'valor' não pode ser vazio");
    }
}
