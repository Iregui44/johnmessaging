# JohnMessaging

Backend demo for processing machinery messages using AWS SQS (with LocalStack for local development).  
This project was created as a technical exercise to demonstrate message ingestion, authorization, deduplication, persistence, and forwarding.

## Features
- Polls messages from an input SQS queue
- Deduplicates based on (sessionGuid, sequenceNumber)
- Validates machine authorization against a REST endpoint
- Persists processed records in a relational database
- Forwards authorized and accepted messages to an output SQS queue
- Marks rejected or duplicate messages without forwarding
- Includes retry logic for SQS publishing failures
- Configurable through Spring Boot properties

## Design Choices
- Model design: Message DTOs are isolated from persistence models to avoid coupling and reduce impact when message structure evolves.
- Testing strategy: Not strict TDD, but tests were added early and extensively at both unit and integration levels.
- Profiles: dev includes mocks for HTTP authorization and queue seeding, while test uses Testcontainers to simulate real services.
- Error handling: Failures in publishing are retried, but transactional consistency can be improved (see below).

## Possible Improvements
- Use a chain of responsibility pattern for message validation and processing
- Replace synchronous AWS SDK client with the async client for higher throughput
- Externalize configuration variables per environment (e.g., via Spring Cloud Config or AWS Parameter Store)
- Improve transactional guarantees: if a message is persisted but not forwarded, it should be rolled back or retried as a unit
- Adopt stricter TDD and CI/CD pipelines
- Use AWS SQS listener templates instead of manual polling

## Requirements
- Docker and Docker Compose installed
- (Optional) JDK 17 and Maven if you want to build and run outside Docker

## Deployment and Running

1. Build the project and supporting services:
   ```bash
   docker-compose build

2. Build the project and supporting services:
   ```bash
   docker-compose up

By default, the project runs with the dev profile, which enables local queue initialization and a mock whitelist-based authorization controller.
This allows testing without connecting to real AWS services or external authentication. But if you want to test real connection and queueing all is based on application-yml properties.

## Test
There are 3 clases of junit tests and one big IT which you can run simultaneusly from any IDE by click run all tests on java folder.