package ua.sinaver.web3.service;

import org.springframework.dao.EmptyResultDataAccessException;
import ua.sinaver.web3.data.SigningKey;
import ua.sinaver.web3.mq.SigningTaskEvent;

public interface ISigningService {
    SigningKey fetchSinginKeyWithRetry() throws EmptyResultDataAccessException;

    void generateAndSaveKeys(int numOfKeys);

    void generateAndSaveRecords(int numOfRecords);

    void signRecordsWithLeastUsedKey(SigningTaskEvent signingTaskEvent);
}
