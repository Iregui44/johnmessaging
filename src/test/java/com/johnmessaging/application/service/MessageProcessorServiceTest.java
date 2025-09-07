package com.johnmessaging.application.service;

import com.johnmessaging.application.dto.MachineMessage;
import com.johnmessaging.application.exception.PublishException;
import com.johnmessaging.domain.model.MessageStatus;
import com.johnmessaging.domain.ports.AuthorizationPort;
import com.johnmessaging.domain.ports.OutQueuePort;
import com.johnmessaging.domain.ports.ProcessedRecordRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class MessageProcessorServiceTest {

    @Test
    void whenFirstTime_thenAccepted_andSecondTime_thenDuplicate() {
        // Arrange
        ProcessedRecordRepository repo = Mockito.mock(ProcessedRecordRepository.class);
        AuthorizationPort auth = mock(AuthorizationPort.class);
        OutQueuePort outQueuePort = mock(OutQueuePort.class);
        MessageProcessorService service = new MessageProcessorService(repo, auth, outQueuePort);
        UUID session = UUID.randomUUID();
        MachineMessage message = new MachineMessage(session, 1L, 42L,
                List.of(new MachineMessage.DataPoint("distance", "m", "100")));
        when(repo.exists(session, 1L)).thenReturn(false);
        when(auth.isAuthorized(42L)).thenReturn(true);

        // Act && Assert
        MessageStatus first = service.process(message);
        assertThat(first).isEqualTo(MessageStatus.ACCEPTED);
        verify(repo).save(session, 1L, 42L, MessageStatus.ACCEPTED);
        verify(outQueuePort).publish(message);

        reset(repo);
        when(repo.exists(session, 1L)).thenReturn(true);

        MessageStatus second = service.process(message);
        assertThat(second).isEqualTo(MessageStatus.DUPLICATE);
        verify(repo, never()).save(any(), anyLong(), anyLong(), any());
        verify(outQueuePort, atMostOnce()).publish(any());
    }

    @Test
    void givenMessage_whenNotAuthorized_thenRejected() {
        // Arrange
        ProcessedRecordRepository repo = mock(ProcessedRecordRepository.class);
        AuthorizationPort auth = mock(AuthorizationPort.class);
        OutQueuePort outQueuePort = mock(OutQueuePort.class);
        MessageProcessorService service = new MessageProcessorService(repo, auth, outQueuePort);
        UUID session = UUID.randomUUID();
        MachineMessage message = new MachineMessage(session, 1L, 7L,
                List.of(new MachineMessage.DataPoint("distance", "m", "100")));
        when(repo.exists(session, 1L)).thenReturn(false);
        when(auth.isAuthorized(7L)).thenReturn(false);

        // Act
        MessageStatus messageStatus = service.process(message);

        // Assert
        assertThat(messageStatus).isEqualTo(MessageStatus.REJECTED);
        verify(repo).save(session, 1L, 7L, MessageStatus.REJECTED);
        verify(repo, never()).save(session, 1L, 7L, MessageStatus.ACCEPTED);
        verify(outQueuePort, never()).publish(any());
    }

    @Test
    void givenMessage_whenAuthorized_thenAccepted() {
        // Arrange
        ProcessedRecordRepository repo = mock(ProcessedRecordRepository.class);
        AuthorizationPort auth = mock(AuthorizationPort.class);
        OutQueuePort outQueuePort = mock(OutQueuePort.class);
        MessageProcessorService service = new MessageProcessorService(repo, auth, outQueuePort);
        UUID session = UUID.randomUUID();
        MachineMessage message = new MachineMessage(session, 1L, 7L,
                List.of(new MachineMessage.DataPoint("distance", "m", "100")));
        when(repo.exists(session, 1L)).thenReturn(false);
        when(auth.isAuthorized(7L)).thenReturn(true);

        // Act
        MessageStatus messageStatus = service.process(message);

        // Assert
        assertThat(messageStatus).isEqualTo(MessageStatus.ACCEPTED);
        verify(repo).save(session, 1L, 7L, MessageStatus.ACCEPTED);
        verify(outQueuePort).publish(message);
    }

    @Test
    void whenPublishFails_thenThrowPublishException_andDoNotPersist() {
        // Arrange
        ProcessedRecordRepository repo = mock(ProcessedRecordRepository.class);
        AuthorizationPort auth = mock(AuthorizationPort.class);
        OutQueuePort out = mock(OutQueuePort.class);
        MessageProcessorService service = new MessageProcessorService(repo, auth, out);
        UUID session = UUID.randomUUID();
        MachineMessage msg = new MachineMessage(session, 1L, 42L,
                List.of(new MachineMessage.DataPoint("distance","m","100")));
        when(repo.exists(session, 1L)).thenReturn(false);
        when(auth.isAuthorized(42L)).thenReturn(true);
        doThrow(new RuntimeException("error")).when(out).publish(msg);

        // Act && Assert
        assertThatThrownBy(() -> service.process(msg))
                .isInstanceOf(PublishException.class)
                .hasMessageContaining("Failed to publish to sqs2");
        verify(repo, never()).save(any(), anyLong(), anyLong(), any());
    }
}