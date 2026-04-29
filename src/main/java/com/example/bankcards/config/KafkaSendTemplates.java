package com.example.bankcards.config;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaSendTemplates {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaSendTemplates(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        kafkaTemplate.send("emailTopic", message);
    }
}