package com.example.vently;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VentlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(VentlyApplication.class, args);
	}

}
