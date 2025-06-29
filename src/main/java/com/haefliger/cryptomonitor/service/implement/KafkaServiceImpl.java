package com.haefliger.cryptomonitor.service.implement;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import com.haefliger.cryptomonitor.service.KafkaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;

import static com.haefliger.cryptomonitor.enums.KafkaEnum.ESTRATEGIA;

/**
 * Author diego-haefliger
 * Date 12/06/25
 */

@Service
@AllArgsConstructor
@Slf4j
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String MENSAGENS_JSON = "mensagens.json";

    @Override
    public void sendMessage(String topic, String key, Object obj) {
        try {
            validateInputs(topic, key, obj);
            String json = serialize(obj);
            log.info("Sending message to topic: {}, key: {}, value: {}", topic, key, json);
            kafkaTemplate.send(topic, key, json);
        } catch (Exception e) {
            log.error("Error preparing to send message: {}", e.getMessage(), e);
            throw new RuntimeException("Error preparing to send message", e);
        }
    }

    @Override
    public void sendMessageEstrategias(TipoIndicadorEnum indicador, String[] parametros) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(MENSAGENS_JSON)) {
            if (is == null) {
                log.error("Resource '{}' not found in the classpath.", MENSAGENS_JSON);
                return;
            }
            JsonNode mensagens = objectMapper.readTree(is);
            String mensagemEstrategia = String.format(mensagens.path(indicador.name()).asText(), (Object[]) parametros);

            kafkaTemplate.send(ESTRATEGIA.getTopic(), indicador.name(), mensagemEstrategia);
            log.info("Mensagem enviada para o tópico {} com chave {}", ESTRATEGIA.getTopic(), indicador.name());
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem para o Kafka: {}", e.getMessage());
        }
    }

    private void validateInputs(String topic, String key, Object obj) {
        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("Topic cannot be null or empty");
        }
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (obj == null) {
            throw new IllegalArgumentException("Object to send cannot be null");
        }
    }

    private String serialize(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

}
