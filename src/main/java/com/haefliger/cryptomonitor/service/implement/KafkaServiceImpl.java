package com.haefliger.cryptomonitor.service.implement;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haefliger.cryptomonitor.service.KafkaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
