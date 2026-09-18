package notification.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import notification.dto.UserEventDto;
import notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationListener {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void listen(String message) {
        try {
            UserEventDto event = objectMapper.readValue(message, UserEventDto.class);

            log.info("Получено событие из Kafka: operation={}, email={}",
                    event.getOperation(), event.getEmail());

            emailService.sendEmail(event.getEmail(), event.getOperation());

        } catch (JsonProcessingException e) {
            log.error("Ошибка парсинга сообщения из Kafka: {}", message, e);
        }
    }
}