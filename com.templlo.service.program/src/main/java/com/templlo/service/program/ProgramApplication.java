package com.templlo.service.program;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ProgramApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProgramApplication.class, args);
	}

}