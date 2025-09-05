package com.johnmessaging.infraestructure.persistence.repository;

import com.johnmessaging.infraestructure.persistence.entity.ProcessedRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedRecordJpaRepo extends JpaRepository<ProcessedRecordEntity, Long> {
    boolean existsBySessionGuidAndSequenceNumber(UUID sessionGuid, long sequenceNumber);
}