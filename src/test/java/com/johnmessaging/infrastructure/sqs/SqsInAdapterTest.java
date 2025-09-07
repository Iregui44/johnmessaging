package com.johnmessaging.infrastructure.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.application.service.MessageProcessorService;
import com.johnmessaging.domain.model.MessageStatus;
import com.johnmessaging.infrastructure.config.AwsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {SqsInAdapter.class, AwsProperties.class, ObjectMapper.class})
class SqsInAdapterTest {

    @MockitoBean
    private SqsClient sqs;

    @MockitoBean
    private MessageProcessorService processor;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AwsProperties props;

    @Autowired
    private SqsInAdapter adapter;

    @Test
    void givenQueueWithMessage_whenPoll_thenMessageProcessedAndDeleted() throws Exception {
        // Arrange
        props.getSqs().setInQueueUrl("http://localhost:4566/000000000000/sqs1");
        MachineMessage msg = new MachineMessage(UUID.randomUUID(), 1L, 42,
                List.of(new MachineMessage.DataPoint("distance", "m", "100")));
        String body = objectMapper.writeValueAsString(msg);
        Message sqsMsg = Message.builder().body(body).receiptHandle("rh-123").build();
        when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(sqsMsg).build());
        when(processor.process(any(MachineMessage.class)))
                .thenReturn(MessageStatus.ACCEPTED);

        // Act
        adapter.poll();

        // Assert
        verify(processor).process(any(MachineMessage.class));
        verify(sqs).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void givenEmptyQueue_whenPoll_thenDoNothing() {
        // Arrange
        props.getSqs().setInQueueUrl("http://localhost:4566/000000000000/sqs1");
        when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(List.of()).build());

        // Act
        adapter.poll();

        // Assert
        verify(processor, never()).process(any());
    }

    @Test
    void givenQueueWithMessage_whenProcessingFails_thenMessageNotDeleted() throws Exception {
        // Arrange
        props.getSqs().setInQueueUrl("http://localhost:4566/000000000000/sqs1");
        MachineMessage msg = new MachineMessage(UUID.randomUUID(), 1L, 42,
                List.of(new MachineMessage.DataPoint("distance", "m", "100")));
        String body = objectMapper.writeValueAsString(msg);
        Message sqsMsg = Message.builder().body(body).receiptHandle("rh-err").build();
        when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(sqsMsg).build());
        when(processor.process(any(MachineMessage.class)))
                .thenThrow(new RuntimeException("boom"));

        // Act
        adapter.poll();

        // Assert
        verify(processor).process(any(MachineMessage.class));
    }

    @Test
    void givenQueueWithMultipleMessages_whenPoll_thenAllProcessedAndDeleted() throws Exception {
        // Arrange
        props.getSqs().setInQueueUrl("http://localhost:4566/000000000000/sqs1");
        MachineMessage msg1 = new MachineMessage(UUID.randomUUID(), 1L, 42,
                List.of(new MachineMessage.DataPoint("d", "m", "10")));
        MachineMessage msg2 = new MachineMessage(UUID.randomUUID(), 2L, 42,
                List.of(new MachineMessage.DataPoint("d", "m", "20")));

        Message sqsMsg1 = Message.builder().body(objectMapper.writeValueAsString(msg1)).receiptHandle("rh-1").build();
        Message sqsMsg2 = Message.builder().body(objectMapper.writeValueAsString(msg2)).receiptHandle("rh-2").build();

        when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(List.of(sqsMsg1, sqsMsg2)).build());
        when(processor.process(any(MachineMessage.class)))
                .thenReturn(MessageStatus.ACCEPTED);

        // Act
        adapter.poll();

        // Assert
        verify(processor, times(2)).process(any(MachineMessage.class));
        verify(sqs, times(2)).deleteMessage(any(DeleteMessageRequest.class));
    }
}