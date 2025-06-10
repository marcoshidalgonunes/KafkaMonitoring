package com.kafkamonitoring.service;

import org.springframework.stereotype.Service;

import com.kafkamonitoring.helper.KafkaAdminHelper;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

@Service
@Slf4j
public class KafkaHealthService {

    @Value("${spring.kafka.bootstrap-servers}")
    private List<String> bootstrapServers;

    @Scheduled(fixedDelayString = "${kafka.health.check.interval:10000}", initialDelayString = "${kafka.health.check.interval:10000}")
    public void isKafkaHealthy() {
        for (String server : bootstrapServers) {
            KafkaAdminHelper kafkaAdminComponent = new KafkaAdminHelper(server);
            if (kafkaAdminComponent.checkHealth()) {
                log.info("Kafka server {} is healthy.", server);  
            } 
        }
    }
}
