package my.code.crmservice.database.repository.outbox;

import my.code.crmservice.database.entity.outbox.OutboxEntity;
import my.code.crmservice.database.entity.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    List<OutboxEntity> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE OutboxEntity e SET e.status = 'PROCESSED', e.processedAt = :now WHERE e.id = :id")
    void markAsProcessed(UUID id, Instant now);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE OutboxEntity e SET e.status = 'FAILED', e.retryCount = e.retryCount + 1 WHERE e.id = :id")
    void markAsFailed(UUID id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM OutboxEntity e WHERE e.status = 'PROCESSED' AND e.processedAt < :cutoff")
    int deleteProcessedBefore(Instant cutoff);
}
