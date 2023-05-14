package ua.sinaver.web3.service;

import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.common.primitives.Bytes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import ua.sinaver.web3.data.SigningKey;
import ua.sinaver.web3.mq.SigningTaskEvent;
import ua.sinaver.web3.data.Record;
import ua.sinaver.web3.repository.RecordRepository;
import ua.sinaver.web3.repository.SigningKeyRepository;

import org.bouncycastle.jcajce.provider.digest.Keccak;

@Service
@Transactional
public class SigningService implements ISigningService {
    public static final Logger LOGGER = LoggerFactory.getLogger(SigningService.class);

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting().create();

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private SigningKeyRepository signingKeyRepository;

    @Autowired
    private EntityManager entityManager;

    // @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000), retryFor =
    // EmptyResultDataAccessException.class)
    @Override
    public SigningKey fetchSinginKeyWithRetry() throws EmptyResultDataAccessException {
        SigningKey signingKey = signingKeyRepository.findFirstByOrderByLastUsedAsc();
        if (signingKey == null) {
            throw new EmptyResultDataAccessException("No Signing Key is available", 1);
        }
        return signingKey;
    }

    @Override
    public void generateAndSaveKeys(int numOfKeys) {
        List<SigningKey> keys = IntStream.range(0, numOfKeys).parallel().mapToObj(num -> {
            SigningKey signingKey = new SigningKey();
            // TODO: for now just seed with random bytes, as signature we will store
            // sha3(data + key)
            signingKey.setKeyData(RandomUtils.nextBytes(32));
            return signingKey;
        }).toList();

        signingKeyRepository.saveAll(keys);
    }

    @Override
    public void generateAndSaveRecords(int numOfRecords) {
        List<Record> records = IntStream.range(0, numOfRecords).parallel().mapToObj(i -> {
            Record record = new Record();
            record.setData(RandomUtils.nextBytes(32));
            return record;
        }).toList();

        recordRepository.saveAll(records);
    }

    @Override
    public void signRecordsWithLeastUsedKey(SigningTaskEvent signingTask) {
        SigningKey signingKey = fetchSinginKeyWithRetry();
        List<Record> recordsInBatch = recordRepository
                .findBySignedFalse(PageRequest.of(0, signingTask.batchSize()));

        MessageDigest digest256 = new Keccak.Digest256();

        recordsInBatch.stream().forEach(r -> {
            r.setSignature(digest256.digest(Bytes.concat(r.getData(), signingKey.getKeyData())));
            r.setSigned(true);
        });

        signingKey.setLastUsed(new Date());

        signingKeyRepository.save(signingKey);
        recordRepository.saveAll(recordsInBatch);

        entityManager.flush();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Batch {} signed by key {} - records: {}",
                    signingTask.taskId(), signingKey.getId(),
                    GSON.toJson(recordsInBatch.stream().map(r -> r.getId()).toList()));
        }
    }
}
