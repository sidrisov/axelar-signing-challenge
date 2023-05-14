package ua.sinaver.web3.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import jakarta.transaction.Transactional;
import ua.sinaver.web3.repository.RecordRepository;
import ua.sinaver.web3.service.ISigningService;;

@Component
@Configuration
public class SigningTaskListener {
    public static final String SIGNING_TASK_QUEUE = "signing-task";

    public static final Logger LOGGER = LoggerFactory.getLogger(SigningTaskListener.class);

    @Autowired
    private ISigningService signingService;

    @Autowired
    public RecordRepository recordRepository;

    @Bean
    Queue queue() {
        return new Queue(SIGNING_TASK_QUEUE, false);
    }

    @Bean
    RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Transactional
    @RabbitListener(queues = "signing-task")
    public void processMessage(SigningTaskEvent event) throws Throwable {
        LOGGER.info("Received signing task: {}", event);
        signingService.signRecordsWithLeastUsedKey(event);
    }
}
