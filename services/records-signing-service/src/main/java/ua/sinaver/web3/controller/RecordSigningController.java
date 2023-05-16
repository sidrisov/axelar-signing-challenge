package ua.sinaver.web3.controller;

import java.util.stream.IntStream;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import ua.sinaver.web3.mq.SigningTaskEvent;
import ua.sinaver.web3.mq.SigningTaskListener;
import ua.sinaver.web3.repository.RecordRepository;

@RestController
@RequestMapping("/signing")
public class RecordSigningController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RecordRepository recordRepository;

    @Value("${service.signing.batch.size}")
    private int batchSize;

    @PostMapping("/batch")
    @Transactional
    public ResponseEntity<String> signData() {
        final long unsignedRecords = recordRepository.countBySignedFalse();
        final int numOfTasks = (int) Math.ceil(unsignedRecords / batchSize);

        IntStream.rangeClosed(1, numOfTasks).forEach(taskId -> {
            rabbitTemplate.convertAndSend(SigningTaskListener.SIGNING_TASK_QUEUE,
                    new SigningTaskEvent(String.valueOf(taskId), batchSize));
        });

        return ResponseEntity
                .ok(String.format("Initiated %s tasks to sign %s records in batches (%s)!", numOfTasks, unsignedRecords,
                        batchSize));
    }

    @GetMapping("/stats")
    public ResponseEntity<String> stats() {
        final long total = recordRepository.count();
        final long signed = recordRepository.countBySignedTrue();

        return ResponseEntity
                .ok(String.format("%s/%s (%s%%)", signed, total, 100 * signed / total));
    }

}
