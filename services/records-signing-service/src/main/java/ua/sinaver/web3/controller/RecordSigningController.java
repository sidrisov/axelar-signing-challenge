package ua.sinaver.web3.controller;

import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.primitives.Bytes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.transaction.Transactional;
import ua.sinaver.web3.data.SigningKey;
import ua.sinaver.web3.repository.RecordRepository;
import ua.sinaver.web3.repository.SigningKeyRepository;

import ua.sinaver.web3.data.Record;

@RestController
@RequestMapping("/signing")
@Transactional
public class RecordSigningController {

    private static final int BATCH_SIZE = 100;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting().create();

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private SigningKeyRepository signingKeyRepository;

    @PostMapping("/batch")
    public ResponseEntity<String> signData() {
        SigningKey signingKey = signingKeyRepository.findFirstByOrderByLastUsedAsc();
        List<Record> recordsInBatch = recordRepository.findBySignedFalse(PageRequest.of(0, BATCH_SIZE));

        MessageDigest digest256 = new Keccak.Digest256();

        recordsInBatch.stream().forEach(r -> {
            r.setSignature(digest256.digest(Bytes.concat(r.getData(), signingKey.getKeyData())));
            r.setSigned(true);
        });

        signingKey.setLastUsed(new Date());

        return ResponseEntity
                .ok(String.format("Batch signed by key %s - records: %s", signingKey.getId(),
                        GSON.toJson(recordsInBatch.stream().map(r -> r.getId()).toList())));
    }

}
