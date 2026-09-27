package gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0)
class GatewayIntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnFallbackWhenServiceIsDown() {
        stubFor(get(urlPathMatching("/api/users/.*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        webTestClient.get().uri("/fallback/user")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").isEqualTo("User Service временно недоступен. Попробуйте позже.");
    }
}