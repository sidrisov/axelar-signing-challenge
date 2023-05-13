package ua.sinaver.web3.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Transactional
class DataIngestorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataIngestorController.class);

    @Value("${service.ingestor.records.size}")
    private int numOfRecords;

    @Value("${service.ingestor.keys.size}")
    private int numOfKeys;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private SigningKeyRepository signingKeyRepository;

    @PostMapping("/records")
    public ResponseEntity<String> generateRecord() {
        List<Record> records = IntStream.range(0, numOfRecords).parallel().mapToObj(i -> {
            Record record = new Record();
            record.setData(RandomUtils.nextBytes(32));
            return record;
        }).toList();

        recordRepository.saveAll(records);

        return ResponseEntity.ok(String.format("%s Records generated!", numOfRecords));
    }

    @PostMapping("/keys")
    public ResponseEntity<String> generateKey() {

        List<SigningKey> keys = IntStream.range(0, numOfKeys).parallel().mapToObj(num -> {
            SigningKey signingKey = new SigningKey();
            // TODO: for now just seed with random bytes, as signature we will store
            // sha3(data + key)
            signingKey.setKeyData(RandomUtils.nextBytes(32));
            return signingKey;
        }).toList();

        signingKeyRepository.saveAll(keys);

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
        leastUsedKey.setLastUsed(new Date());
        return ResponseEntity.ok(String.format("Least Used Key: %s",
                leastUsedKey.getId()));
    }
}
