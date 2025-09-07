package com.johnmessaging.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public class AwsProperties {
    private String region;
    private String endpoint;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    private Sqs sqs = new Sqs();

    public Sqs getSqs() {
        return sqs;
    }

    public void setSqs(Sqs sqs) {
        this.sqs = sqs;
    }

    public static class Sqs {
        private String inQueueUrl;
        private String outQueueUrl;

        public String getInQueueUrl() {
            return inQueueUrl;
        }

        public void setInQueueUrl(String inQueueUrl) {
            this.inQueueUrl = inQueueUrl;
        }

        public String getOutQueueUrl() {
            return outQueueUrl;
        }

        public void setOutQueueUrl(String outQueueUrl) {
            this.outQueueUrl = outQueueUrl;
        }
    }
}
