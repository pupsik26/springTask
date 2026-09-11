package notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
        String subject;
        String text;

        if ("CREATE".equalsIgnoreCase(operation)) {
            subject = "Регистрация на сайте " + siteName;
            text = "Здравствуйте! Ваш аккаунт на сайте " + siteName + " был успешно создан.";
        } else if ("DELETE".equalsIgnoreCase(operation)) {
            subject = "Ваш аккаунт удалён";
            text = "Здравствуйте! Ваш аккаунт был удалён.";
        } else {
            log.warn("Неизвестная операция: {}", operation);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
        log.info("Письмо успешно отправлено на [{}]: {}", toEmail, text);
    }
}