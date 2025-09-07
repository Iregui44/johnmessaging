package com.johnmessaging.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ProcessedRecord {
    private final UUID sessionGuid;
    private final long sequenceNumber;
    private final long machineId;
    private final MessageStatus status;
    private final Instant createdAt;

    public ProcessedRecord(UUID sessionGuid, long sequenceNumber, long machineId, MessageStatus status, Instant createdAt) {
        this.sessionGuid = sessionGuid;
        this.sequenceNumber = sequenceNumber;
        this.machineId = machineId;
        this.status = status;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    // getters
    public UUID sessionGuid() {
        return sessionGuid;
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    public long machineId() {
        return machineId;
    }

    public MessageStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
