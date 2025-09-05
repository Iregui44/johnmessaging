package com.johnmessaging.application.service;

import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.domain.ports.ProcessedRecordRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProcessorService {

    private final ProcessedRecordRepository processedRepo;

    public void process(@Valid MachineMessage msg) {

    }
}