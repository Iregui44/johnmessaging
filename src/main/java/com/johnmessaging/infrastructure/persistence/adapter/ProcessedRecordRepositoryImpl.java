package com.johnmessaging.infrastructure.persistence.adapter;

import com.johnmessaging.domain.model.MessageStatus;
import com.johnmessaging.domain.ports.ProcessedRecordRepository;
import com.johnmessaging.infrastructure.persistence.entity.ProcessedRecordEntity;
import com.johnmessaging.infrastructure.persistence.repository.ProcessedRecordJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProcessedRecordRepositoryImpl implements ProcessedRecordRepository {

    private final ProcessedRecordJpaRepo repo;

    @Override
    public boolean exists(UUID sessionGuid, long sequenceNumber) {
        return repo.existsBySessionGuidAndSequenceNumber(sessionGuid, sequenceNumber);
    }

    @Override
    public void save(UUID sessionGuid, long sequenceNumber, long machineId, MessageStatus status) {
        repo.saveAndFlush(ProcessedRecordEntity.builder()
                .sessionGuid(sessionGuid)
                .sequenceNumber(sequenceNumber)
                .machineId(machineId)
                .status(status)
                .build());
    }

}
