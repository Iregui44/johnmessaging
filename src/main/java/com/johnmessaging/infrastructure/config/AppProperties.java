package com.johnmessaging.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Auth auth = new Auth();

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public static class Auth {
        private String baseUrl;

        private List<Long> whitelist;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public List<Long> getWhitelist() {
            return whitelist;
        }

        public void setWhitelist(List<Long> whitelist) {
            this.whitelist = whitelist;
        }
    }
}
