package ua.sinaver.web3.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ua.sinaver.web3.repository")
@EntityScan(basePackages = "ua.sinaver.web3.data")
@ComponentScan(basePackages = "ua.sinaver.web3")
@RestController
public class RecordsSigningApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecordsSigningApplication.class, args);
	}

	@PostMapping("/health")
	public ResponseEntity<String> generate() {
		return ResponseEntity.ok("All good!");
	}
}
