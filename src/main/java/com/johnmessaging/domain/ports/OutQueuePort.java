package com.johnmessaging.domain.ports;

import com.johnmessaging.application.dto.MachineMessage;

public interface OutQueuePort {
    void publish(MachineMessage message);
}
