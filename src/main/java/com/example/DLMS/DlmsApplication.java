package com.example.DLMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DlmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(DlmsApplication.class, args);
	}

}
