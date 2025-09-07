package com.johnmessaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class JohnMessagingApplication {

	public static void main(String[] args) {
		SpringApplication.run(JohnMessagingApplication.class, args);
	}

}
