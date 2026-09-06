package userservice.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendEmail(String email, String operation) {
        String message;
        if ("CREATE".equalsIgnoreCase(operation)) {
            message = "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
        } else if ("DELETE".equalsIgnoreCase(operation)) {
            message = "Здравствуйте! Ваш аккаунт был удалён.";
        } else {
            message = "Неизвестная операция.";
        }

        log.info(">>> [EMAIL-SERVICE] ОТПРАВКА EMAIL на [{}] | Текст: {}", email, message);
    }
}