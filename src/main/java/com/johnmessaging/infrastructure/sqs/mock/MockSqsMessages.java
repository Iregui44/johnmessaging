package com.johnmessaging.infrastructure.sqs.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.infrastructure.config.AwsProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;
import java.util.UUID;

@Component
@DependsOn("sqsAwsMockInitializer")
@Profile("dev")
@Slf4j
public class MockSqsMessages {
    private final SqsClient sqs;
    private final AwsProperties props;
    private final ObjectMapper objectMapper;

    public MockSqsMessages(SqsClient sqs, AwsProperties props, ObjectMapper objectMapper) {
        this.sqs = sqs;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void populateInQueue() {
        try {
            String inUrl = props.getSqs().getInQueueUrl();
            UUID uuid1 = UUID.randomUUID();
            seed(inUrl, new MachineMessage(
                    uuid1, 1, 1,
                    List.of(new MachineMessage.DataPoint("distance", "m", "100"))));
            seed(inUrl, new MachineMessage(
                    uuid1, 1, 1,
                    List.of(new MachineMessage.DataPoint("distance", "m", "100"))));
            seed(inUrl, new MachineMessage(
                    UUID.randomUUID(), 1, 4,
                    List.of(new MachineMessage.DataPoint("distance", "m", "100"))));
        } catch (Exception e) {
            log.error("Error populating SQS with seed messages", e);
        }
    }

    private void seed(String queueUrl, MachineMessage message) throws JsonProcessingException {
        var body = objectMapper.writeValueAsString(message);
        sqs.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .build());
        log.info("Seeded message session={} seq={} machineId={}",
                message.sessionGuid(), message.sequenceNumber(), message.machineId());
    }
}
