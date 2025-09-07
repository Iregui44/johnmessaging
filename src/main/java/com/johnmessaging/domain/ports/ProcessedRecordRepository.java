package com.johnmessaging.domain.ports;

import com.johnmessaging.domain.model.MessageStatus;

import java.util.UUID;

public interface ProcessedRecordRepository {
    boolean exists(UUID sessionGuid, long sequenceNumber);
    void save(UUID sessionGuid, long sequenceNumber, long machineId, MessageStatus status);
}
