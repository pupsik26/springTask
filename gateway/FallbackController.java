package com.example.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/user")
    public ResponseEntity<Map<String, String>> userServiceFallback() {
        return ResponseEntity.status(503)
                .body(Map.of("error", "User Service временно недоступен. Попробуйте позже."));
    }

    @GetMapping("/fallback/notification")
    public ResponseEntity<Map<String, String>> notificationServiceFallback() {
        return ResponseEntity.status(503)
                .body(Map.of("error", "Notification Service временно недоступен."));
    }
}