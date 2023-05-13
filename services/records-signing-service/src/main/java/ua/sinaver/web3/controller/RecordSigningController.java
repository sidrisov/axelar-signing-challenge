package ua.sinaver.web3.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import ua.sinaver.web3.mq.SigningTaskListener;

@Transactional
@RestController
@RequestMapping("/signing")
public class RecordSigningController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/batch")
    public ResponseEntity<String> signData() {

        rabbitTemplate.convertAndSend(SigningTaskListener.SIGNING_TASK_QUEUE, "Sign data in batch!");

        return ResponseEntity
                .ok("Signing Task initiated!");
    }

}
