package com.johnmessaging.application.service;

import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.domain.model.MessageStatus;
import com.johnmessaging.domain.ports.ProcessedRecordRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessageProcessorServiceTest {

    @Test
    void whenFirstTime_thenAccepted_andSecondTime_thenDuplicate() {
        // Arrange
        ProcessedRecordRepository repo = Mockito.mock(ProcessedRecordRepository.class);
        MessageProcessorService service = new MessageProcessorService(repo);
        UUID session = UUID.randomUUID();
        MachineMessage msg = new MachineMessage(session, 1L, 42L,
                List.of(new MachineMessage.DataPoint("distance", "m", "100")));
        when(repo.exists(session, 1L)).thenReturn(false);

        // Act
        MessageStatus first = service.process(msg);
        assertThat(first).isEqualTo(MessageStatus.ACCEPTED);
        verify(repo).save(session, 1L, 42L, MessageStatus.ACCEPTED);

        reset(repo);
        when(repo.exists(session, 1L)).thenReturn(true);

        MessageStatus second = service.process(msg);
        assertThat(second).isEqualTo(MessageStatus.DUPLICATE);
        verify(repo, never()).save(any(), anyLong(), anyLong(), any());
    }
}