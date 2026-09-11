package notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import notification.dto.NotificationRequest;
import notification.dto.UserEventDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"user-events"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, UserEventDto> kafkaTemplate;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void sendDirectNotification_create_shouldSendEmailWithCorrectText() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setEmail("test@example.com");
        request.setOperation("CREATE");

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals("test@example.com", sentMessage.getTo()[0]);
        assertTrue(sentMessage.getText().contains("был успешно создан"));
    }

    @Test
    void kafkaListener_onCreateEvent_shouldSendEmail() throws Exception {
        UserEventDto event = new UserEventDto("CREATE", "kafka-create@example.com");

        kafkaTemplate.send("user-events", event).get();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender, atLeastOnce()).send(captor.capture());

            SimpleMailMessage sentMessage = captor.getValue();
            assertEquals("kafka-create@example.com", sentMessage.getTo()[0]);
            assertTrue(sentMessage.getText().contains("был успешно создан"));
        });
    }

    @Test
    void kafkaListener_onDeleteEvent_shouldSendEmail() throws Exception {
        UserEventDto event = new UserEventDto("DELETE", "kafka-delete@example.com");

        kafkaTemplate.send("user-events", event).get();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender, atLeastOnce()).send(captor.capture());

            SimpleMailMessage sentMessage = captor.getValue();
            assertEquals("kafka-delete@example.com", sentMessage.getTo()[0]);
            assertTrue(sentMessage.getText().contains("был удалён"));
        });
    }
}