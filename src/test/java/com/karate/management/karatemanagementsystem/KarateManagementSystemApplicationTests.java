package com.karate.management.karatemanagementsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class KarateManagementSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
