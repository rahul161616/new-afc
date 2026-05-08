package com.jugger.afc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AfcApplication {

	public static void main(String[] args) {
		SpringApplication.run(AfcApplication.class, args);
	}

}
