package com.kafkamonitoring.helper;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;

@Slf4j
public class KafkaAdminHelper implements AutoCloseable {

    private final AdminClient adminClient;

    public KafkaAdminHelper(String bootstrapServer) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServer);
        this.adminClient = AdminClient.create(props);
    }
    
    private List<String> getGroupIds() throws Exception {
        return adminClient.listConsumerGroups()
            .all()
            .get()
            .stream()
            .map(groupListing -> groupListing.groupId())
            .collect(Collectors.toList());
    }

    public List<String> getTopicNames() throws InterruptedException, ExecutionException {
        return new ArrayList<>(adminClient.listTopics().names().get());
    }    

    private Collection<TopicPartition> getTopicPartitions(String topic) throws Exception {
        DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(topic));
        TopicDescription description = result.topicNameValues().get(topic).get();
        Collection<TopicPartition> partitions = new ArrayList<>();
        description.partitions().forEach(p -> partitions.add(new TopicPartition(topic, p.partition())));
        return partitions;
    }

    private Map<TopicPartition, Long> getEndOffsets(Collection<TopicPartition> partitions) throws ExecutionException, InterruptedException {
        Map<TopicPartition, OffsetSpec> request = new HashMap<>();
        for (TopicPartition tp : partitions) {
            request.put(tp, OffsetSpec.latest());
        }
        ListOffsetsResult result = adminClient.listOffsets(request);
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> offsets = result.all().get();
        Map<TopicPartition, Long> endOffsets = new HashMap<>();
        for (Map.Entry<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> entry : offsets.entrySet()) {
            endOffsets.put(entry.getKey(), entry.getValue().offset());
        }
        return endOffsets;
    }

    private Map<TopicPartition, Long> getCommittedOffsets(String groupId) throws ExecutionException, InterruptedException {
        ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(groupId);
        Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> offsets = offsetsResult.partitionsToOffsetAndMetadata().get();
        Map<TopicPartition, Long> committedOffsets = new HashMap<>();
        for (Map.Entry<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> entry : offsets.entrySet()) {
            committedOffsets.put(entry.getKey(), entry.getValue().offset());
        }
        return committedOffsets;
    }

    public Map<TopicPartition, Long> getLag(String groupId, Collection<TopicPartition> partitions) throws ExecutionException, InterruptedException {
        Map<TopicPartition, Long> endOffsets = getEndOffsets(partitions);
        Map<TopicPartition, Long> committedOffsets = getCommittedOffsets(groupId);
        Map<TopicPartition, Long> lag = new HashMap<>();
        for (TopicPartition tp : partitions) {
            long end = endOffsets.getOrDefault(tp, 0L);
            long committed = committedOffsets.getOrDefault(tp, 0L);
            lag.put(tp, end - committed);
        }
        return lag;
    }

    @Override
    public void close() {
        adminClient.close();
    }
}