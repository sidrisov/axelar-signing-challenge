package ua.sinaver.web3.recordssigningservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/")
public class RecordsSigningServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecordsSigningServiceApplication.class, args);
	}

	@PostMapping("/generate")
	public ResponseEntity<String> generate() {
		return ResponseEntity.ok("All good!");
	}
}
