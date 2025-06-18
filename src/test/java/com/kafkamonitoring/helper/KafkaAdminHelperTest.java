package com.kafkamonitoring.helper;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class KafkaAdminHelperTest {

    @Mock
    private AdminClient adminClient;

    private KafkaAdminHelper helper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        helper = new KafkaAdminHelper("localhost:9092");
        // Inject mock AdminClient via reflection
        var field = KafkaAdminHelper.class.getDeclaredField("adminClient");
        field.setAccessible(true);
        field.set(helper, adminClient);
    }

    @Test
    void testGetTopicNames() throws Exception {
        ListTopicsResult listTopicsResult = mock(ListTopicsResult.class);
        when(adminClient.listTopics()).thenReturn(listTopicsResult);
        Set<String> topics = new HashSet<>(Arrays.asList("topic1", "topic2"));
        when(listTopicsResult.names()).thenReturn(KafkaFuture.completedFuture(topics));

        List<String> result = helper.getTopicNames();
        assertEquals(2, result.size());
        assertTrue(result.contains("topic1"));
        assertTrue(result.contains("topic2"));
    }

    @Test
    void testGetLag() throws Exception {
        // Prepare partitions
        TopicPartition tp = new TopicPartition("topic1", 0);
        Collection<TopicPartition> partitions = List.of(tp);

        // Mock getEndOffsets
        Map<TopicPartition, OffsetSpec> offsetSpecMap = Map.of(tp, OffsetSpec.latest());
        ListOffsetsResult listOffsetsResult = mock(ListOffsetsResult.class);
        when(adminClient.listOffsets(anyMap())).thenReturn(listOffsetsResult);

        ListOffsetsResult.ListOffsetsResultInfo offsetsResultInfo = mock(ListOffsetsResult.ListOffsetsResultInfo.class);
        when(offsetsResultInfo.offset()).thenReturn(100L);

        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> offsetsInfo = Map.of(tp, offsetsResultInfo);       
        when(listOffsetsResult.all()).thenReturn(KafkaFuture.completedFuture(offsetsInfo));

        // Mock getCommittedOffsets
        ListConsumerGroupOffsetsResult offsetsResult = mock(ListConsumerGroupOffsetsResult.class);
        when(adminClient.listConsumerGroupOffsets(anyString())).thenReturn(offsetsResult);
        Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata> committed = Map.of(
                tp, new org.apache.kafka.clients.consumer.OffsetAndMetadata(90L)
        );
        when(offsetsResult.partitionsToOffsetAndMetadata())
            .thenReturn(KafkaFuture.completedFuture(committed));
        when(listOffsetsResult.all()).thenReturn(KafkaFuture.completedFuture(offsetsInfo));

        Map<TopicPartition, Long> lag = helper.getLag("group1", partitions);
        assertEquals(1, lag.size());
        assertEquals(10L, lag.get(tp));
    }

    @Test
    void testClose() {
        helper.close();
        verify(adminClient, times(1)).close();
    }
}