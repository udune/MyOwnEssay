package com.example.myownessay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MyownessayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyownessayApplication.class, args);
	}

}
