package ca.etsmtl.taf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import java.time.Duration;

// Non utilisé
@Configuration
public class WebConfigSelenium {

    @Value("${taf.app.selenium_container_url}")
    String SELENIUM_CONTAINER_URL;

    @Value("${taf.app.selenium_container_port}")
    String SELENIUM_CONTAINER_PORT;

    @Bean
    public WebClient webClient() {
        // Configure connection pool to support parallel requests
        ConnectionProvider connectionProvider = ConnectionProvider.builder("selenium-pool")
                .maxConnections(50)  // Support up to 50 concurrent connections
                .pendingAcquireMaxCount(100)  // Allow queue of 100 waiting requests
                .pendingAcquireTimeout(Duration.ofSeconds(60))  // Wait up to 60s for connection
                .build();
        
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofMinutes(5));  // Selenium tests can take time
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(SELENIUM_CONTAINER_URL + ":" + SELENIUM_CONTAINER_PORT)
                .defaultCookie("cookie-name", "cookie-value")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}