package com.johnmessaging.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient authRestClient(AppProperties props) {
        return RestClient.builder()
                .baseUrl(props.getAuth().getBaseUrl())
                .build();
    }
}
