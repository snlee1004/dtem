package com.example.dtem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DtemApplication {

	public static void main(String[] args) {
		SpringApplication.run(DtemApplication.class, args);
	}

}
