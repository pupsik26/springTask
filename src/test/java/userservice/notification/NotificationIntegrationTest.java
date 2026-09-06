package userservice.notification;

import userservice.notification.dto.NotificationRequest;
import userservice.notification.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private EmailService emailService;

    @Test
    void sendDirectNotification_createOperation_shouldCallEmailServiceWithCorrectText() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setEmail("test@example.com");
        request.setOperation("CREATE");

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(emailService, times(1)).sendEmail("test@example.com", "CREATE");
    }

    @Test
    void sendDirectNotification_deleteOperation_shouldCallEmailServiceWithCorrectText() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setEmail("delete@example.com");
        request.setOperation("DELETE");

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(emailService, times(1)).sendEmail("delete@example.com", "DELETE");
    }
}