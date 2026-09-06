package userservice.notification.listener;

import userservice.dto.UserEventDto;
import userservice.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationListener {

    private final EmailService emailService;

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void listen(UserEventDto event) {
        log.info("Получено событие из Kafka: operation={}, email={}", event.getOperation(), event.getEmail());
        emailService.sendEmail(event.getEmail(), event.getOperation());
    }
}