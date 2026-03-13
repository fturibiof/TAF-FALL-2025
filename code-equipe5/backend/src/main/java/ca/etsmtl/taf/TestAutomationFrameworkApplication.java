// src/main/java/ca/etsmtl/taf/TestAutomationFrameworkApplication.java
package ca.etsmtl.taf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // disabled

@SpringBootApplication
// @EnableJpaAuditing // disabled
public class TestAutomationFrameworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestAutomationFrameworkApplication.class, args);
	}

}
