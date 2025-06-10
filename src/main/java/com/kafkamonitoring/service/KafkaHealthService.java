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
            try (KafkaAdminHelper kafkaAdminComponent = new KafkaAdminHelper(server)) {
                log.info("Checking health of Kafka server: {}", server);
                
                // Check if the server is reachable and can list topics
                List<String> topics = kafkaAdminComponent.getTopicNames();
                if (!topics.isEmpty()) {
                    log.info("Kafka server {} is healthy.", topics.size(), server);
                }
            } catch (Exception e) {
                log.error("Kafka server {} is not healthy!", server, e);
            }
        }
    }
}
