package notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.dto.OperationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.from-email}")
    private String fromEmail;

    @Value("${app.site-name}")
    private String siteName;

    public void sendEmail(String toEmail, String operation) {
        OperationType type;
        try {
            type = OperationType.fromString(operation);
        } catch (IllegalArgumentException e) {
            log.error("Неизвестная операция '{}'. Письмо не отправлено на [{}]", operation, toEmail);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(type.getSubject(siteName));
        message.setText(type.getText(siteName));

        try {
            mailSender.send(message);
            log.info("Письмо успешно отправлено на [{}] | Тема: {}", toEmail, message.getSubject());
        } catch (MailException e) {
            log.error("Не удалось отправить письмо на [{}] для операции {}. " +
                    "Причина: {}", toEmail, operation, e.getMessage(), e);
        }
    }
}