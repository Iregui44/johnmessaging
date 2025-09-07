package com.johnmessaging.application.service;

import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.application.exception.PublishException;
import com.johnmessaging.domain.model.MessageStatus;
import com.johnmessaging.domain.ports.AuthorizationPort;
import com.johnmessaging.domain.ports.OutQueuePort;
import com.johnmessaging.domain.ports.ProcessedRecordRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProcessorService {

    private final ProcessedRecordRepository processedRepo;

    private final AuthorizationPort authorization;

    private final OutQueuePort outQueue;

    public MessageStatus process(@Valid MachineMessage message) {
        if (processedRepo.exists(message.sessionGuid(), message.sequenceNumber())) {
            log.debug("Message with session id: {}, sequence Number: {} and machineId: {} was DUPLICATED", message.sessionGuid(), message.sequenceNumber(), message.machineId());
            return MessageStatus.DUPLICATE;
        }
        if (!authorization.isAuthorized(message.machineId())) {
            log.debug("Message with session id: {}, sequence Number: {} and machineId: {} was REJECTED", message.sessionGuid(), message.sequenceNumber(), message.machineId());
            processedRepo.save(message.sessionGuid(), message.sequenceNumber(), message.machineId(), MessageStatus.REJECTED);
            return MessageStatus.REJECTED;
        }
        try {
            outQueue.publish(message);
        } catch (Exception ex) {
            throw new PublishException("Failed to publish to sqs2", ex);
        }
        log.debug("Message with session id: {}, sequence Number: {} and machineId: {} was ACCEPTED", message.sessionGuid(), message.sequenceNumber(), message.machineId());
        processedRepo.save(message.sessionGuid(), message.sequenceNumber(), message.machineId(), MessageStatus.ACCEPTED);
        return MessageStatus.ACCEPTED;
    }
}