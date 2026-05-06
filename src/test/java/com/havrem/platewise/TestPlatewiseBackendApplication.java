package com.havrem.platewise;

import org.springframework.boot.SpringApplication;

public class TestPlatewiseBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(PlatewiseBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
