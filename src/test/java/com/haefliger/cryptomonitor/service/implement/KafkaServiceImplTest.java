package com.haefliger.cryptomonitor.service.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaServiceImpl — caracterização do tópico, chave e corpo publicados")
class KafkaServiceImplTest {

    @Mock private Emitter<String> emitter;

    private KafkaServiceImpl service() {
        return new KafkaServiceImpl(emitter, new ObjectMapper());
    }

    private Message<String> mensagemPublicada() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Message<String>> captor = ArgumentCaptor.forClass(Message.class);
        verify(emitter).send(captor.capture());
        return captor.getValue();
    }

    private static void assertTopicoEChave(Message<String> mensagem, String topico, String chave) {
        OutgoingKafkaRecordMetadata<?> metadata =
                mensagem.getMetadata(OutgoingKafkaRecordMetadata.class).orElseThrow();
        assertThat(metadata.getTopic()).isEqualTo(topico);
        assertThat(metadata.getKey()).isEqualTo(chave);
    }

    @Test
    @DisplayName("mensagem de estratégia sai no tópico \"strategy\" com o indicador como chave")
    void sendMessageEstrategiasPreco() {
        service().sendMessageEstrategias(TipoIndicadorEnum.PRECO, new String[] {"BTCUSDT", "101"});

        Message<String> mensagem = mensagemPublicada();
        assertTopicoEChave(mensagem, "strategy", "PRECO");
        assertThat(mensagem.getPayload())
                .isEqualTo("💰 Estratégia de Preço acionada para o ativo BTCUSDT valor $101");
    }

    @Test
    void sendMessageEstrategiasRsi() {
        service()
                .sendMessageEstrategias(
                        TipoIndicadorEnum.RSI, new String[] {"BTCUSDT", "< 80", "60m"});

        Message<String> mensagem = mensagemPublicada();
        assertTopicoEChave(mensagem, "strategy", "RSI");
        assertThat(mensagem.getPayload())
                .isEqualTo(
                        "🏆 Estratégia de RSI acionada para o ativo BTCUSDT valor RSI < 80 intervalo 60m");
    }

    @Test
    @DisplayName("parâmetros de menos engolem a falha e nada é publicado")
    void parametrosInsuficientesNaoPublicam() {
        service().sendMessageEstrategias(TipoIndicadorEnum.RSI, new String[] {"BTCUSDT"});

        verifyNoInteractions(emitter);
    }
}
