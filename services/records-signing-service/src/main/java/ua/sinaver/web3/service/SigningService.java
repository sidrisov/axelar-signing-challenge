package ua.sinaver.web3.service;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.transaction.Transactional;
import ua.sinaver.web3.data.SigningKey;
import ua.sinaver.web3.mq.SigningTaskEvent;
import ua.sinaver.web3.data.Record;
import ua.sinaver.web3.repository.RecordRepository;
import ua.sinaver.web3.repository.SigningKeyRepository;

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
    private ICryptoService cryptoService;

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
            try {
                signingKey.setKeyData(cryptoService.generateECPrivateKey());
            } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
                LOGGER.error("Error", e);
                throw new RuntimeException(e);
            }
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

        recordsInBatch.stream().forEach(r -> {
            try {
                byte[] signature = cryptoService.signDataWithECPrivateKey(r.getData(), signingKey.getKeyData());
                r.setSignature(signature);
                r.setSigned(true);
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException
                    | SignatureException e) {
                LOGGER.error("Error", e);
                throw new RuntimeException(e);
            }
        });

        signingKey.setLastUsed(new Date());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Batch {} signed by key {} - records: {}",
                    signingTask.taskId(), signingKey.getId(),
                    GSON.toJson(recordsInBatch.stream().map(r -> r.getId()).toList()));
        }
    }
}
