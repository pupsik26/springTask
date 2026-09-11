package notification.dto;

import lombok.Data;

@Data
public class NotificationRequest {
    private String email;
    private String operation;
}