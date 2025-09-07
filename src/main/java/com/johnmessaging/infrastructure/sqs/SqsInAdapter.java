package com.johnmessaging.infrastructure.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.application.service.MessageProcessorService;
import com.johnmessaging.domain.model.MessageStatus;
import com.johnmessaging.infrastructure.config.AppProperties;
import com.johnmessaging.infrastructure.config.AwsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Component
@Slf4j
public class SqsInAdapter {

    private final SqsClient sqs;
    private final ObjectMapper objectMapper;
    private final MessageProcessorService processor;
    private final String inUrl;

    public SqsInAdapter(SqsClient sqs, ObjectMapper om, MessageProcessorService processor, AwsProperties props) {
        this.sqs = sqs;
        this.objectMapper = om;
        this.processor = processor;
        this.inUrl = props.getSqs().getInQueueUrl();
    }

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        log.debug("Start polling");
        ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                .queueUrl(inUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10)
                .build();

        List<Message> messages = sqs.receiveMessage(req).messages();
        if (messages == null || messages.isEmpty()) return;

        for (Message message : messages) {
            try {
                MachineMessage machineMessage = objectMapper.readValue(message.body(), MachineMessage.class);
                MessageStatus messageStatus = processor.process(machineMessage);
                deleteMessage(message.receiptHandle());
                log.debug("Message forwarded to sqs2 and deleted from sqs1");
            } catch (Exception ex) {
                log.debug("Message with error");
            }
        }
    }

    private void deleteMessage(String receiptHandle) {
        sqs.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(inUrl)
                .receiptHandle(receiptHandle)
                .build());
    }
}
