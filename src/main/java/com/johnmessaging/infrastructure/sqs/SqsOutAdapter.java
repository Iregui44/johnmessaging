package com.johnmessaging.infrastructure.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.domain.ports.OutQueuePort;
import com.johnmessaging.infrastructure.config.AppProperties;
import com.johnmessaging.infrastructure.config.AwsProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class SqsOutAdapter implements OutQueuePort {

    private final SqsClient sqs;

    private final ObjectMapper objectMapper;
    private final String outUrl;

    public SqsOutAdapter(SqsClient sqs, ObjectMapper om, AwsProperties props) {
        this.sqs = sqs;
        this.objectMapper = om;
        this.outUrl = props.getSqs().getOutQueueUrl();
    }

    @Override
    public void publish(MachineMessage message) {
        try {
            String body = objectMapper.writeValueAsString(message);
            sqs.sendMessage(SendMessageRequest.builder()
                    .queueUrl(outUrl)
                    .messageBody(body)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error publishing to sqs2", e);
        }
    }
}
