package com.johnmessaging.infrastructure.http;

import com.johnmessaging.domain.ports.AuthorizationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class AuthorizationHttpAdapter implements AuthorizationPort {

    private final RestClient restClient;

    public AuthorizationHttpAdapter(RestClient authRestClient) {
        this.restClient = authRestClient;
    }

    private record AuthorizationResponse(boolean authorized) {}

    @Override
    public boolean isAuthorized(long machineId) {
        try {
            log.debug("Calling authorization service for machineId={}", machineId);
            AuthorizationResponse body = restClient.get()
                    .uri("/api/machines/{id}/authorized", machineId)
                    .retrieve()
                    .body(AuthorizationResponse.class);
            log.debug("Authorization response for machineId={} -> {}", machineId, body);
            return body.authorized();
        } catch (Exception e) {
            log.error("Error calling authorization service for machineId=" + machineId, e);
            throw e;
        }
    }
}
