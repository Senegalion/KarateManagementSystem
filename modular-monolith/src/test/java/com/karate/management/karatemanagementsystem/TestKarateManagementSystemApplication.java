package com.karate.management.karatemanagementsystem;

import org.springframework.boot.SpringApplication;

public class TestKarateManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(KarateManagementSystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
