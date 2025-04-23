package com.neryn.norman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NormanApplication {

	public static void main(String[] args) {
		SpringApplication.run(NormanApplication.class, args);
    }

}
