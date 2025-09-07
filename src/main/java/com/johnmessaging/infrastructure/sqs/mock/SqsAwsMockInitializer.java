package com.johnmessaging.infrastructure.sqs.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnmessaging.infrastructure.config.AwsProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;

import java.net.URI;

@Component("sqsAwsMockInitializer")
@Profile({"dev", "test"})
@Slf4j
public class SqsAwsMockInitializer {
    private final SqsClient sqs;
    private final AwsProperties props;
    private final ObjectMapper objectMapper;

    public SqsAwsMockInitializer(SqsClient sqs, AwsProperties props, ObjectMapper om) {
        this.sqs = sqs;
        this.props = props;
        this.objectMapper = om;
    }

    @PostConstruct
    public void ensureQueues() throws JsonProcessingException {
        ensureQueue(props.getSqs().getInQueueUrl());
        ensureQueue(props.getSqs().getOutQueueUrl());
    }

    private void ensureQueue(String queueUrl) {
        String name = extractQueueName(queueUrl);
        try {
            sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(name).build());
            log.info("SQS queue '{}' already exists", name);
        } catch (QueueDoesNotExistException e) {
            log.info("SQS queue '{}' doe snot exist, creating…", name);
            sqs.createQueue(CreateQueueRequest.builder().queueName(name).build());
            log.info("SQS queue '{}' created", name);
        }
    }

    private String extractQueueName(String queueUrl) {
        URI uri = URI.create(queueUrl);
        String path = uri.getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
