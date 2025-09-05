package com.johnmessaging.application.service;

import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.domain.model.MessageStatus;
import com.johnmessaging.domain.ports.AuthorizationPort;
import com.johnmessaging.domain.ports.ProcessedRecordRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProcessorService {

    private final ProcessedRecordRepository processedRepo;

    private final AuthorizationPort authorization;

    public MessageStatus process(@Valid MachineMessage msg) {
        if (processedRepo.exists(msg.sessionGuid(), msg.sequenceNumber())) {
            return MessageStatus.DUPLICATE;
        }

        if (!authorization.isAuthorized(msg.machineId())) {
            processedRepo.save(msg.sessionGuid(), msg.sequenceNumber(), msg.machineId(), MessageStatus.REJECTED);
            return MessageStatus.REJECTED;
        }

        processedRepo.save(msg.sessionGuid(), msg.sequenceNumber(), msg.machineId(), MessageStatus.ACCEPTED);
        return MessageStatus.ACCEPTED;
    }
}