package com.redhat.cajun.navy.incident.service;

import static org.mockito.Mockito.mock;

import com.redhat.cajun.navy.incident.message.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

@Profile("test")
@Configuration
public class ConfigurationTests {

    @Primary
    @SuppressWarnings("unchecked")
    @Bean(name = "kafkaTemplate")
    public KafkaTemplate<String, Message<?>> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

}
