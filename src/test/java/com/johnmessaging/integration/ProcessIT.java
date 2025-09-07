// src/test/java/com/johnmessaging/integration/SqsFlowIT.java
package com.johnmessaging.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnmessaging.JohnMessagingApplication;
import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.domain.model.MessageStatus;
import com.johnmessaging.domain.model.ProcessedRecord;
import com.johnmessaging.domain.ports.ProcessedRecordRepository;
import com.johnmessaging.infrastructure.config.AwsProperties;
import com.johnmessaging.infrastructure.persistence.entity.ProcessedRecordEntity;
import com.johnmessaging.infrastructure.persistence.repository.ProcessedRecordJpaRepo;
import com.johnmessaging.infrastructure.sqs.SqsInAdapter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class ProcessIT {

    @Container
    static LocalStackContainer localstack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                    .withServices(LocalStackContainer.Service.SQS)
                    .withEnv("AWS_DEFAULT_REGION", "eu-west-1");


    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("aws.region", () -> "eu-west-1");
        r.add("aws.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        r.add("aws.sqs.in-queue-url", () -> localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString() + "/000000000000/sqs1");
        r.add("aws.sqs.out-queue-url", () -> localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString() + "/000000000000/sqs2");
        r.add("app.auth.whitelist", () -> "42");
    }

    @Autowired
    SqsClient sqs;
    @Autowired
    AwsProperties props;
    @Autowired
    ObjectMapper om;

    @Autowired
    ProcessedRecordJpaRepo processedRepo;

    @Autowired
    SqsInAdapter sqsInAdapter;

    private String inUrl() {
        return props.getSqs().getInQueueUrl();
    }

    private String outUrl() {
        return props.getSqs().getOutQueueUrl();
    }

    private void sendToIn(Object payload) throws Exception {
        String body = (payload instanceof String s) ? s : om.writeValueAsString(payload);
        sqs.sendMessage(SendMessageRequest.builder().queueUrl(inUrl()).messageBody(body).build());
    }

    private List<Message> readOut() {
        return sqs.receiveMessage(ReceiveMessageRequest.builder()
                        .queueUrl(outUrl())
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(1)
                        .build())
                .messages();
    }

    private List<Message> readInNoWait() {
        return sqs.receiveMessage(ReceiveMessageRequest.builder()
                        .queueUrl(inUrl())
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(0)
                        .build())
                .messages();
    }

    private void eventuallyAssert(Duration timeout, Runnable assertion) throws InterruptedException {
        long end = System.currentTimeMillis() + timeout.toMillis();
        AssertionError last = null;
        do {
            try {
                assertion.run();
                return;
            } catch (AssertionError ae) {
                last = ae;
                Thread.sleep(150);
            }
        } while (System.currentTimeMillis() < end);
        if (last != null) throw last;

    }

    @Test
    void givenMessage_whenProcessed_thenAcceptedAndForwardedAndPersisted() throws Exception {
        // Arrange
        UUID session = UUID.randomUUID();
        MachineMessage msg = new MachineMessage(session, 1L, 42,
                List.of(new MachineMessage.DataPoint("distance", "m", "100")));
        sendToIn(msg);

        // Act
        sqsInAdapter.poll();

        // Assert
        eventuallyAssert(Duration.ofSeconds(5), () ->
                assertThat(readOut()).anySatisfy(m -> {
                    try {
                        var got = om.readValue(m.body(), new TypeReference<MachineMessage>() {
                        });
                        assertThat(got.sessionGuid()).isEqualTo(session);
                        assertThat(got.sequenceNumber()).isEqualTo(1L);
                        assertThat(got.machineId()).isEqualTo(42L);
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                })
        );
        eventuallyAssert(Duration.ofSeconds(5), () -> {
            Optional<ProcessedRecordEntity> found =
                    processedRepo.findBySessionGuidAndSequenceNumber(session, 1L);
            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(MessageStatus.ACCEPTED);
        });
        assertThat(readInNoWait()).isEmpty();
    }

    @Test
    void givenMessage_whenProcessed_thenRejectedAndNotRepublish() throws Exception {
        // Arrange
        UUID session = UUID.randomUUID();
        MachineMessage msg = new MachineMessage(session, 1L, 99,
                List.of(new MachineMessage.DataPoint("distance", "m", "100")));
        sendToIn(msg);

        // Act
        sqsInAdapter.poll();

        // Assert
        eventuallyAssert(Duration.ofSeconds(10), () ->
                assertThat(readInNoWait()).isEmpty()
        );
        eventuallyAssert(Duration.ofSeconds(10), () -> {
            Optional<ProcessedRecordEntity> found =
                    processedRepo.findBySessionGuidAndSequenceNumber(session, 1L);
            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(MessageStatus.REJECTED);
        });
        assertThat(readOut()).isEmpty();
    }

    @Test
    void givenMessage_whenProcessed_thenDuplicateIsFiltered() throws Exception {
        // Arrange
        UUID session = UUID.randomUUID();
        MachineMessage m1 = new MachineMessage(session, 1L, 42,
                List.of(new MachineMessage.DataPoint("distance", "m", "100")));
        MachineMessage m2 = new MachineMessage(session, 1, 42, m1.data());
        sendToIn(m1);
        sendToIn(m2);

        // Act
        sqsInAdapter.poll();

        // Assert
        eventuallyAssert(Duration.ofSeconds(20), () ->
                assertThat(readOut()).hasSize(1)
        );
        eventuallyAssert(Duration.ofSeconds(20), () -> {
            Optional<ProcessedRecordEntity> found =
                    processedRepo.findBySessionGuidAndSequenceNumber(session, 1L);
            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(MessageStatus.ACCEPTED);
        });
        assertThat(readInNoWait()).isEmpty();
    }
}