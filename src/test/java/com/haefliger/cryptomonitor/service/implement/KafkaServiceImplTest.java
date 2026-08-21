package com.haefliger.cryptomonitor.service.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaServiceImpl — caracterização do tópico, chave e corpo publicados")
class KafkaServiceImplTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaServiceImpl service() {
        return new KafkaServiceImpl(kafkaTemplate, new ObjectMapper());
    }

    @Test
    @DisplayName("mensagem de estratégia sai no tópico \"strategy\" com o indicador como chave")
    void sendMessageEstrategiasPreco() {
        service().sendMessageEstrategias(TipoIndicadorEnum.PRECO, new String[] {"BTCUSDT", "101"});

        verify(kafkaTemplate).send("strategy", "PRECO",
                "💰 Estratégia de Preço acionada para o ativo BTCUSDT valor $101");
    }

    @Test
    void sendMessageEstrategiasRsi() {
        service().sendMessageEstrategias(TipoIndicadorEnum.RSI, new String[] {"BTCUSDT", "< 80", "60m"});

        verify(kafkaTemplate).send("strategy", "RSI",
                "🏆 Estratégia de RSI acionada para o ativo BTCUSDT valor RSI < 80 intervalo 60m");
    }

    @Test
    @DisplayName("parâmetros de menos engolem a falha e nada é publicado")
    void parametrosInsuficientesNaoPublicam() {
        service().sendMessageEstrategias(TipoIndicadorEnum.RSI, new String[] {"BTCUSDT"});

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void sendMessageSerializaObjetoComoJson() {
        service().sendMessage("qualquer", "chave", java.util.Map.of("a", 1));

        verify(kafkaTemplate).send("qualquer", "chave", "{\"a\":1}");
    }

    @Test
    @DisplayName("tópico, chave ou objeto nulo viram RuntimeException, não IllegalArgumentException")
    void validacaoDeEntrada() {
        assertThatThrownBy(() -> service().sendMessage(null, "chave", "x"))
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service().sendMessage("topico", "", "x"))
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service().sendMessage("topico", "chave", null))
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }
}
