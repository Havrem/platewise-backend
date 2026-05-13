package com.havrem.platewise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PlatewiseBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlatewiseBackendApplication.class, args);
	}

}
