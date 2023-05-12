package ua.sinaver.web3.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import ua.sinaver.web3.data.Record;
import ua.sinaver.web3.data.SigningKey;
import ua.sinaver.web3.repository.RecordRepository;
import ua.sinaver.web3.repository.SigningKeyRepository;

@RestController
@RequestMapping("/ingestor")
class DataIngestorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataIngestorController.class);

    private static final int NUM_RECORDS = 100;
    private static final int NUM_KEYS = 100;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private SigningKeyRepository signingKeyRepository;

    @PostMapping("/records")
    @Transactional
    public ResponseEntity<String> generateRecord() {
        List<Record> records = IntStream.range(0, NUM_RECORDS).parallel().mapToObj(i -> {
            Record record = new Record();
            record.setData(RandomUtils.nextBytes(32));
            return record;
        }).toList();

        recordRepository.saveAll(records);

        return ResponseEntity.ok(String.format("%s Records generated!", NUM_RECORDS));
    }

    @PostMapping("/keys")
    @Transactional
    public ResponseEntity<String> generateKey() {

        List<SigningKey> keys = IntStream.range(0, NUM_KEYS).parallel().mapToObj(num -> {
            SigningKey signingKey = new SigningKey();
            // TODO: for now just seed with random bytes, as signature we will store
            // sha3(data + key)
            signingKey.setKeyData(RandomUtils.nextBytes(32));
            return signingKey;
        }).toList();

        signingKeyRepository.saveAll(keys);

        return ResponseEntity.ok(String.format("%s Signing Keys generated!", NUM_KEYS));
    }

    @GetMapping("/records")
    @Transactional
    public ResponseEntity<String> records() {
        return ResponseEntity.ok(String.format("Records: %s",
                recordRepository.findBySignedFalse(PageRequest.of(0, 150)).stream().map(r -> r.getId()).toList()));
    }

    @GetMapping("/keys")
    @Transactional
    public ResponseEntity<String> keys() {
        SigningKey leastUsedKey = signingKeyRepository.findFirstByOrderByLastUsedAsc();
        leastUsedKey.setLastUsed(new Date());
        return ResponseEntity.ok(String.format("Least Used Key: %s",
                leastUsedKey.getId()));
    }
}

// MessageDigest digest256 = new Keccak.Digest256();
// return digest256.digest(data);
