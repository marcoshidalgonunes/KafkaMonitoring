package com.kafkamonitoring.helper;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.admin.AdminClient;

@Slf4j
public class KafkaAdminHelper {

    private final String bootstrapServer;

    public KafkaAdminHelper(String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    private AdminClient createAdminClient() {
        java.util.Properties props = new java.util.Properties();
        props.put("bootstrap.servers", bootstrapServer);
        return AdminClient.create(props);
    }

    public boolean checkHealth() {
        try (AdminClient adminClient = createAdminClient()) {
            adminClient.listTopics().names().get();
            return true;
        } catch (Exception e) {
            log.error("Kafka server {} is not healthy: {}", bootstrapServer, e.getMessage());
            return false;
        }
    }
}