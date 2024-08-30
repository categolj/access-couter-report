package org.example.hellospringbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HelloSpringBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloSpringBatchApplication.class, args);
	}

}
