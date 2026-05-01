package my.code.crmservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Читає kafka.topic.* з application.yml — type-safe конфігурація.
// Альтернатива @Value("${kafka.topic.client-created}") на кожному полі — менш зручно.
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka.topic")
public class KafkaTopicProperties {

    // Відповідає kafka.topic.client-created в application.yml
    private String clientCreated;

    // Відповідає kafka.topic.deal-closed в application.yml
    private String dealClosed;
}