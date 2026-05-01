package my.code.crmservice.service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.code.crmservice.database.entity.client.Client;
import my.code.crmservice.database.entity.deal.Deal;
import my.code.crmservice.database.entity.outbox.OutboxEntity;
import my.code.crmservice.database.repository.outbox.OutboxRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

// OutboxService НЕ має @Transactional — він завжди викликається
// всередині транзакції сервісу (ClientServiceImpl, DealServiceImpl).
// Транзакція сервісу "обгортає" і save(entity) і saveOutbox() — атомарно.
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    // Назви event типів — константи щоб уникнути typo в рядках
    public static final String CLIENT_CREATED = "CLIENT_CREATED";
    public static final String DEAL_CLOSED = "DEAL_CLOSED";

    private final OutboxRepository outboxRepository;
    // ObjectMapper — Jackson для серіалізації payload в JSON рядок
    private final ObjectMapper objectMapper;

    public void publishClientCreated(Client client) {
        // Payload: мінімально необхідні дані для downstream сервісів
        // billing потребує: clientId, userId
        // notifications потребує: userId, name, email
        Map<String, Object> payload = Map.of(
                "clientId", client.getId().toString(),
                "userId", client.getUserId(),
                "name", client.getName(),
                "email", client.getEmail() != null ? client.getEmail() : ""
        );
        saveOutboxEvent(CLIENT_CREATED, payload);
    }

    public void publishDealClosed(Deal deal) {
        // billing потребує: dealId, clientId, value, currency, status (WON/LOST)
        Map<String, Object> payload = Map.of(
                "dealId", deal.getId().toString(),
                "clientId", deal.getClient().getId().toString(),
                "title", deal.getTitle(),
                "value", deal.getValue() != null ? deal.getValue().toString() : "0",
                "currency", deal.getCurrency(),
                "status", deal.getStatus().name(),
                "closedAt", deal.getClosedAt().toString()
        );
        saveOutboxEvent(DEAL_CLOSED, payload);
    }

    private void saveOutboxEvent(String eventType, Map<String, Object> payload) {
        try {
            // Серіалізуємо payload в JSON рядок для зберігання в jsonb колонці
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEntity outboxEntity = OutboxEntity.builder()
                    .eventType(eventType)
                    .payload(payloadJson)
                    .build(); // status=PENDING, retryCount=0 — defaults в entity

            outboxRepository.save(outboxEntity);
            log.debug("Outbox event saved: type={}", eventType);

        } catch (JsonProcessingException e) {
            // Серіалізація не може провалитись для простого Map<String, Object>,
            // але обробляємо щоб не swallow помилку мовчки
            throw new IllegalStateException("Failed to serialize outbox payload for event: " + eventType, e);
        }
    }
}
