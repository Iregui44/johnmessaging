// src/test/java/com/johnmessaging/infrastructure/sqs/SqsOutAdapterRetryTest.java
package com.johnmessaging.infrastructure.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.infrastructure.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class SqsOutAdapterRetryTest {

    @MockitoBean
    SqsClient sqsClient;

    @Autowired
    ObjectMapper om;

    @Autowired
    AppProperties props;

    @Autowired
    SqsOutAdapter adapter;

    @Test
    void givenMessage_whenPublish_thenRetriesAndSucceedsOnThirdAttempt() throws Exception {
        // Arrange
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("boom1"))
                .thenThrow(new RuntimeException("boom2"))
                .thenReturn(SendMessageResponse.builder().messageId("ok").build());
        var msg = new MachineMessage(
                UUID.randomUUID(), 1, 42,
                List.of(new MachineMessage.DataPoint("distance", "m", "100"))
        );

        // Act
        adapter.publish(msg);

        // Assert
        verify(sqsClient, times(3)).sendMessage(any(SendMessageRequest.class));
    }


    @Test
    void givenMessage_whenPublish_thenFailsAfterAllRetries() {
        // Arrange
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("always"));
        var msg = new MachineMessage(UUID.randomUUID(), 1, 42,
                List.of(new MachineMessage.DataPoint("distance", "m", "100")));

        // Act && Assert
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> adapter.publish(msg))
                .isInstanceOf(RuntimeException.class);
        verify(sqsClient, atLeast(3)).sendMessage(any(SendMessageRequest.class));
    }
}