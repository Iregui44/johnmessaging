package com.johnmessaging.domain.ports;

import com.johnmessaging.application.dto.MachineMessage;

import java.util.function.Consumer;

public interface InQueuePort {
    void startConsuming(Consumer<MachineMessage> handler);
}
