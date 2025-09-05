package com.johnmessaging.application.service;

import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.domain.model.MessageStatus;
import com.johnmessaging.domain.ports.ProcessedRecordRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProcessorService {

    private final ProcessedRecordRepository processedRepo;

    public MessageStatus process(@Valid MachineMessage msg) {
        boolean already = processedRepo.exists(msg.sessionGuid(), msg.sequenceNumber());
        if (already) {
            return MessageStatus.DUPLICATE;
        }
        processedRepo.save(msg.sessionGuid(), msg.sequenceNumber(), msg.machineId(), MessageStatus.ACCEPTED);
        return MessageStatus.ACCEPTED;
    }
}