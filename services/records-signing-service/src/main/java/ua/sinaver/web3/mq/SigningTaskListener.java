package ua.sinaver.web3.mq;

import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.google.common.primitives.Bytes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.transaction.Transactional;
import ua.sinaver.web3.data.SigningKey;
import ua.sinaver.web3.repository.RecordRepository;
import ua.sinaver.web3.repository.SigningKeyRepository;
import ua.sinaver.web3.data.Record;;

@Component
@Configuration
public class SigningTaskListener {
    public static final String SIGNING_TASK_QUEUE = "signing-task";
    private static final Logger LOGGER = LoggerFactory.getLogger(SigningTaskListener.class);

    private static final int BATCH_SIZE = 10000;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting().create();

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private SigningKeyRepository signingKeyRepository;

    @Bean
    Queue queue() {
        return new Queue(SIGNING_TASK_QUEUE, false);
    }

    @Transactional
    @RabbitListener(queues = "signing-task")
    public void processMessage(String content) throws Throwable {
        LOGGER.info("Received signing task: {}", content);

        SigningKey signingKey = signingKeyRepository.findFirstByOrderByLastUsedAsc();
        List<Record> recordsInBatch = recordRepository.findBySignedFalse(PageRequest.of(0, BATCH_SIZE));

        MessageDigest digest256 = new Keccak.Digest256();

        recordsInBatch.stream().forEach(r -> {
            r.setSignature(digest256.digest(Bytes.concat(r.getData(), signingKey.getKeyData())));
            r.setSigned(true);
        });

        signingKey.setLastUsed(new Date());

        LOGGER.info("Batch signed by key {} - records: {}", signingKey.getId(),
                GSON.toJson(recordsInBatch.stream().map(r -> r.getId()).toList()));
    }
}
