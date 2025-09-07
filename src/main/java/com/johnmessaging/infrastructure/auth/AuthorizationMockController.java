package com.johnmessaging.infrastructure.auth;

import com.johnmessaging.infrastructure.config.AppProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Profile({"dev", "test"})
@RequestMapping("/api/machines")
public class AuthorizationMockController {

    private final AppProperties props;

    public AuthorizationMockController(AppProperties props) {
        this.props = props;
    }

    @GetMapping("/{id}/authorized")
    public Map<String, Boolean> authorized(@PathVariable("id") long machineId) {
        boolean allowed = props.getAuth().getWhitelist() != null
                && props.getAuth().getWhitelist().contains(machineId);
        return Map.of("authorized", allowed);
    }
}