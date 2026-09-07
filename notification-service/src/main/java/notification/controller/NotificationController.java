package notification.controller;

import notification.dto.NotificationRequest;
import notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendDirectNotification(@RequestBody NotificationRequest request) {
        emailService.sendEmail(request.getEmail(), request.getOperation());
        return ResponseEntity.ok("Сообщение успешно отправлено на " + request.getEmail());
    }
}