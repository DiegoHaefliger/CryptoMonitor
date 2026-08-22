package com.haefliger.cryptomonitor.service.implement;

import static com.haefliger.cryptomonitor.enums.KafkaEnum.ESTRATEGIA;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.service.KafkaService;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class KafkaServiceImpl implements KafkaService {

    private static final Logger log = LoggerFactory.getLogger(KafkaServiceImpl.class);

    private static final String MENSAGENS_JSON = "mensagens.json";

    private final Emitter<String> emitter;
    private final ObjectMapper objectMapper;

    KafkaServiceImpl(@Channel("estrategia") Emitter<String> emitter, ObjectMapper objectMapper) {
        this.emitter = emitter;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendMessageEstrategias(TipoIndicadorEnum indicador, String[] parametros) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(MENSAGENS_JSON)) {
            if (is == null) {
                log.error("Resource '{}' not found in the classpath.", MENSAGENS_JSON);
                return;
            }
            JsonNode mensagens = objectMapper.readTree(is);
            String mensagemEstrategia =
                    String.format(mensagens.path(indicador.name()).asText(), (Object[]) parametros);

            publicar(ESTRATEGIA.getTopic(), indicador.name(), mensagemEstrategia);
            log.info(
                    "Mensagem enviada para o tópico {} com chave {}",
                    ESTRATEGIA.getTopic(),
                    indicador.name());
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem para o Kafka: {}", e.getMessage());
        }
    }

    private void publicar(String topic, String key, String payload) {
        emitter.send(
                Message.of(payload)
                        .addMetadata(
                                OutgoingKafkaRecordMetadata.<String>builder()
                                        .withTopic(topic)
                                        .withKey(key)
                                        .build()));
    }
}
