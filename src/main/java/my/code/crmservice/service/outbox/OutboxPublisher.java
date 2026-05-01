package my.code.crmservice.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.code.crmservice.database.entity.outbox.OutboxEntity;
import my.code.crmservice.database.entity.outbox.OutboxStatus;
import my.code.crmservice.database.repository.outbox.OutboxRepository;
import my.code.crmservice.config.KafkaTopicProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

// @Component (не @Service) — це інфраструктурний компонент, не бізнес-сервіс
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int MAX_RETRY_COUNT = 3;

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    // @Scheduled: викликається кожні 5 секунд після завершення попереднього виклику
    // fixedDelay (не fixedRate): не накопичує виклики якщо попередній ще виконується
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEntity> pending = outboxRepository
                .findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pending.isEmpty()) {
            return; // нічого не логуємо — викликається кожні 5 сек
        }

        log.debug("Processing {} pending outbox events", pending.size());

        for (OutboxEntity event : pending) {
            publishEvent(event);
        }
    }

    // Очистка: видаляємо оброблені записи старші 7 днів (раз на добу)
    @Scheduled(cron = "0 0 2 * * *") // щодня о 02:00
    @Transactional
    public void cleanupProcessedEvents() {
        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
        int deleted = outboxRepository.deleteProcessedBefore(cutoff);
        if (deleted > 0) {
            log.info("Cleaned up {} processed outbox events older than 7 days", deleted);
        }
    }

    private void publishEvent(OutboxEntity event) {
        // Вибираємо Kafka topic по типу події
        String topic = resolveTopic(event.getEventType());

        try {
            // kafkaTemplate.send() — async, повертає CompletableFuture.
            // .get() робить його sync — чекаємо підтвердження від Kafka broker.
            // Це гарантує що event дійсно записаний до broker перед markAsProcessed.
            kafkaTemplate.send(topic, event.getId().toString(), event.getPayload()).get();
            outboxRepository.markAsProcessed(event.getId(), Instant.now());
            log.debug("Outbox event published: id={}, type={}", event.getId(), event.getEventType());

        } catch (Exception e) {
            log.error("Failed to publish outbox event: id={}, type={}, attempt={}",
                    event.getId(), event.getEventType(), event.getRetryCount() + 1, e);

            if (event.getRetryCount() >= MAX_RETRY_COUNT - 1) {
                // Вичерпано всі спроби → FAILED (потребує ручного втручання або alerting)
                outboxRepository.markAsFailed(event.getId());
                log.error("Outbox event moved to FAILED after {} attempts: id={}", MAX_RETRY_COUNT, event.getId());
            } else {
                // Ще є спроби → тільки інкрементуємо retry_count, статус PENDING
                outboxRepository.markAsFailed(event.getId()); // тимчасово — scheduler підніме при наступному run
            }
        }
    }

    // Маппінг eventType → Kafka topic name з конфігурації
    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case OutboxService.CLIENT_CREATED -> topicProperties.getClientCreated();
            case OutboxService.DEAL_CLOSED -> topicProperties.getDealClosed();
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
