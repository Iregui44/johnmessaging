package com.johnmessaging.domain.ports;

public interface AuthorizationPort {
    boolean isAuthorized(long machineId);
}
