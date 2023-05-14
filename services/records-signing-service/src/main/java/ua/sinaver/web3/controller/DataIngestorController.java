package ua.sinaver.web3.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import ua.sinaver.web3.data.SigningKey;
import ua.sinaver.web3.repository.RecordRepository;
import ua.sinaver.web3.repository.SigningKeyRepository;
import ua.sinaver.web3.service.ISigningService;

@RestController
@RequestMapping("/ingestor")
@Transactional
class DataIngestorController {
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
        signingService.generateAndSaveRecords(numOfRecords);
        return ResponseEntity.ok(String.format("%s Records generated!", numOfRecords));
    }

    @PostMapping("/keys")
    public ResponseEntity<String> generateKeys() {
        signingService.generateAndSaveKeys(numOfRecords);
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
