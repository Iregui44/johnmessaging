package com.johnmessaging.infrastructure.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.infrastructure.config.AppProperties;
import com.johnmessaging.infrastructure.config.AwsProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Component
@Profile("dev")
@Slf4j
public class SqsAwsInitializer {
    private final SqsClient sqs;
    private final AwsProperties props;
    private final ObjectMapper objectMapper;

    public SqsAwsInitializer(SqsClient sqs, AwsProperties props, ObjectMapper om) {
        this.sqs = sqs;
        this.props = props;
        this.objectMapper = om;
    }

    @PostConstruct
    public void ensureQueues() throws JsonProcessingException {
        ensureQueue(props.getSqs().getInQueueUrl());
        ensureQueue(props.getSqs().getOutQueueUrl());
        populateInQueue();
    }

    private void ensureQueue(String queueUrl) {
        String name = extractQueueName(queueUrl);
        try {
            sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(name).build());
            log.info("SQS queue '{}' ya existe", name);
        } catch (QueueDoesNotExistException e) {
            log.info("SQS queue '{}' no existe, creando…", name);
            sqs.createQueue(CreateQueueRequest.builder().queueName(name).build());
            log.info("SQS queue '{}' creada", name);
        }
    }

    private String extractQueueName(String queueUrl) {
        URI uri = URI.create(queueUrl);
        String path = uri.getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    private void populateInQueue() throws JsonProcessingException {
        UUID session1 = UUID.randomUUID();
        MachineMessage message = new MachineMessage(
                session1,
                1,
                1,
                List.of(new MachineMessage.DataPoint("distance", "m", "100"))
        );
        String inUrl = props.getSqs().getInQueueUrl();
        String body = objectMapper.writeValueAsString(message);
        sqs.sendMessage(SendMessageRequest.builder()
                .queueUrl(inUrl)
                .messageBody(body)
                .build());
        MachineMessage message2 = new MachineMessage(
                session1,
                1,
                1,
                List.of(new MachineMessage.DataPoint("distance", "m", "100"))
        );
        String body2 = objectMapper.writeValueAsString(message2);
        sqs.sendMessage(SendMessageRequest.builder()
                .queueUrl(inUrl)
                .messageBody(body2)
                .build());
        UUID session2 = UUID.randomUUID();
        MachineMessage message3 = new MachineMessage(
                session2,
                1,
                4,
                List.of(new MachineMessage.DataPoint("distance", "m", "100"))
        );
        String body3 = objectMapper.writeValueAsString(message3);
        sqs.sendMessage(SendMessageRequest.builder()
                .queueUrl(inUrl)
                .messageBody(body2)
                .build());

    }
}
