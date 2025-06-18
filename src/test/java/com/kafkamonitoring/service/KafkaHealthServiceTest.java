package com.kafkamonitoring.service;

import com.kafkamonitoring.helper.KafkaAdminHelper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.Arrays;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KafkaHealthServiceTest {

    @InjectMocks
    private KafkaHealthService kafkaHealthService;

    @BeforeEach
    void setUp() throws Exception {
        // Inject test bootstrap servers via reflection
        var field = KafkaHealthService.class.getDeclaredField("bootstrapServers");
        field.setAccessible(true);
        field.set(kafkaHealthService, Arrays.asList("server1:9092", "server2:9092"));
    }

    @Test
    void testIsKafkaHealthy_AllHealthy() throws InterruptedException, ExecutionException {
        try (MockedConstruction<KafkaAdminHelper> mocked = mockConstruction(KafkaAdminHelper.class,
                (mock, context) -> when(mock.getTopicNames()).thenReturn(List.of("topicA", "topicB")))) {
            kafkaHealthService.isKafkaHealthy();
            // Verify getTopicNames called for each server
            List<KafkaAdminHelper> constructed = mocked.constructed();
            assertEquals(2, constructed.size());
            for (KafkaAdminHelper helper : constructed) {
                verify(helper).getTopicNames();
                verify(helper).close();
            }
        }
    }

    @Test
    void testIsKafkaHealthy_OneUnhealthy() throws InterruptedException, ExecutionException {
        try (MockedConstruction<KafkaAdminHelper> mocked = mockConstruction(KafkaAdminHelper.class,
                (mock, context) -> {
                    if (context.arguments().get(0).equals("server1:9092")) {
                        when(mock.getTopicNames()).thenThrow(new RuntimeException("Kafka down"));
                    } else {
                        when(mock.getTopicNames()).thenReturn(List.of("topicA"));
                    }
                })) {
            kafkaHealthService.isKafkaHealthy();
            List<KafkaAdminHelper> constructed = mocked.constructed();
            assertEquals(2, constructed.size());
            verify(constructed.get(0)).getTopicNames();
            verify(constructed.get(1)).getTopicNames();
            verify(constructed.get(0)).close();
            verify(constructed.get(1)).close();
        }
    }

    @Test
    void testIsKafkaHealthy_EmptyTopics() throws InterruptedException, ExecutionException {
        try (MockedConstruction<KafkaAdminHelper> mocked = mockConstruction(KafkaAdminHelper.class,
                (mock, context) -> when(mock.getTopicNames()).thenReturn(Collections.emptyList()))) {
            kafkaHealthService.isKafkaHealthy();
            List<KafkaAdminHelper> constructed = mocked.constructed();
            assertEquals(2, constructed.size());
            for (KafkaAdminHelper helper : constructed) {
                verify(helper).getTopicNames();
                verify(helper).close();
            }
        }
    }
}