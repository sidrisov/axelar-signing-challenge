package ua.sinaver.web3.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import ua.sinaver.web3.service.ISigningService;

@RestController
@RequestMapping("/ingestor")
@Transactional
class DataIngestorController {
    public static final Logger LOGGER = LoggerFactory.getLogger(DataIngestorController.class);

    @Value("${service.ingestor.records.size}")
    private int numOfRecords;

    @Value("${service.ingestor.keys.size}")
    private int numOfKeys;

    @Autowired
    private ISigningService signingService;

    @PostMapping("/records")
    public ResponseEntity<String> generateRecords() {
        try {
            signingService.generateAndSaveRecords(numOfRecords);
        } catch (Throwable t) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate records!", t);
        }
        return ResponseEntity.ok(String.format("%s Records generated!", numOfRecords));
    }

    @PostMapping("/keys")
    public ResponseEntity<String> generateKeys() {
        try {
            signingService.generateAndSaveKeys(numOfKeys);
        } catch (Throwable t) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate keys!", t);
        }
        return ResponseEntity.ok(String.format("%s Signing Keys generated!", numOfKeys));
    }
}
