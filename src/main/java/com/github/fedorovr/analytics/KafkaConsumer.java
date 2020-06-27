package com.github.fedorovr.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fedorovr.analytics.dto.KafkaPaymentInfo;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KafkaConsumer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter
    private final Map<String, List<KafkaPaymentInfo>> paymentsByUser = new HashMap<>();

    @KafkaListener(topics = "RAW_PAYMENTS")
    public void consume(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        KafkaPaymentInfo info;
        try {
            info = objectMapper.readValue(message, KafkaPaymentInfo.class);
            logger.debug("New payment info received={}", info);
            paymentsByUser.putIfAbsent(info.getUserId(), new ArrayList<>());
            paymentsByUser.get(info.getUserId()).add(info);
        } catch (JsonProcessingException e) {
            logger.warn("JsonProcessingException", e);
        }
    }
}
