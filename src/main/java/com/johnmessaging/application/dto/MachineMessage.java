package com.johnmessaging.application.dto;

import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record MachineMessage(
        @NotNull UUID sessionGuid,
        @Positive long sequenceNumber,
        @Positive long machineId,
        @NotNull @Size(min = 1) List<DataPoint> data
) {
    public record DataPoint(
            @NotBlank String type,
            @NotBlank String unit,
            @NotBlank String value
    ) {
    }
}
