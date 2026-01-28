package com.mars.ec.email;

import com.mars.ec.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;

    // 監聽 Queue，一有訊息就執行
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(String to) {
        System.out.println("RabbitMQ received message: Send email to " + to);
        emailService.sendSimpleEmail(to);
    }
}