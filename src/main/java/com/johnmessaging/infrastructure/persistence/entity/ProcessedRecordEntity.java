package com.johnmessaging.infrastructure.persistence.entity;

import com.johnmessaging.domain.model.MessageStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "processed_message",
        uniqueConstraints = @UniqueConstraint(name = "uq_processed_msg",
                columnNames = {"session_guid","sequence_number"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessedRecordEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="session_guid", nullable=false, columnDefinition="uuid")
    private UUID sessionGuid;

    @Column(name="sequence_number", nullable=false)
    private long sequenceNumber;

    @Column(name="machine_id", nullable=false)
    private long machineId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private MessageStatus status;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @PrePersist void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
