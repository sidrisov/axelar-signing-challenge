package ua.sinaver.web3.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import ua.sinaver.web3.data.SigningKey;
import ua.sinaver.web3.repository.RecordRepository;
import ua.sinaver.web3.repository.SigningKeyRepository;
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
    private RecordRepository recordRepository;

    @Autowired
    private SigningKeyRepository signingKeyRepository;

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

    @ExceptionHandler(Error.class)
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

    @GetMapping("/records")
    public ResponseEntity<String> records() {
        return ResponseEntity.ok(String.format("Records: %s",
                recordRepository.findBySignedFalse(PageRequest.of(0, 150)).stream().map(r -> r.getId()).toList()));
    }

    @GetMapping("/keys")
    public ResponseEntity<String> keys() {
        SigningKey leastUsedKey = signingKeyRepository.findFirstByOrderByLastUsedAsc();
        return ResponseEntity.ok(String.format("Least Used Key: %s",
                leastUsedKey.getId()));
    }
}
