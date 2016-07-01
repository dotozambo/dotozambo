package com.dotozambo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DotozamboApplication {

	@RequestMapping("/heart_beat")
	public String healthCheck() {
		return "Server Alive";
	}
	
	@RequestMapping("/")
    public String index(){

		return "Welcome to Dotozambo";
    }
		
	public static void main(String[] args) {
		SpringApplication.run(DotozamboApplication.class, args);
	}
}
