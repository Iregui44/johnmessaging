package com.johnmessaging.application.service;

import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.domain.model.MessageStatus;
import com.johnmessaging.domain.ports.AuthorizationPort;
import com.johnmessaging.domain.ports.OutQueuePort;
import com.johnmessaging.domain.ports.ProcessedRecordRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProcessorService {

    private final ProcessedRecordRepository processedRepo;

    private final AuthorizationPort authorization;

    private final OutQueuePort outQueue;

    public MessageStatus process(@Valid MachineMessage message) {
        if (processedRepo.exists(message.sessionGuid(), message.sequenceNumber())) {
            return MessageStatus.DUPLICATE;
        }

        if (!authorization.isAuthorized(message.machineId())) {
            processedRepo.save(message.sessionGuid(), message.sequenceNumber(), message.machineId(), MessageStatus.REJECTED);
            return MessageStatus.REJECTED;
        }

        processedRepo.save(message.sessionGuid(), message.sequenceNumber(), message.machineId(), MessageStatus.ACCEPTED);
        outQueue.publish(message);
        return MessageStatus.ACCEPTED;
    }
}