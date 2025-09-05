package com.johnmessaging.infrastructure.http;

import com.johnmessaging.domain.ports.AuthorizationPort;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthorizationHttpAdapter implements AuthorizationPort {

    private final RestClient restClient;

    public AuthorizationHttpAdapter(RestClient authRestClient) {
        this.restClient = authRestClient;
    }

    private record AuthorizationResponse(boolean authorized) {}

    @Override
    public boolean isAuthorized(long machineId) {
        AuthorizationResponse body = restClient.get()
                .uri("/api/machines/{id}/authorized", machineId)
                .retrieve()
                .body(AuthorizationResponse.class);
        return body.authorized();
    }
}
