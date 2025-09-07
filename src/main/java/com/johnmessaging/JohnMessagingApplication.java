package com.johnmessaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRetry
public class JohnMessagingApplication {

	public static void main(String[] args) {
		SpringApplication.run(JohnMessagingApplication.class, args);
	}

}
