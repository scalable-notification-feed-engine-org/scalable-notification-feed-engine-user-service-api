package com.activity_hub.notification_feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UserServiceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApiApplication.class, args);
	}

}
