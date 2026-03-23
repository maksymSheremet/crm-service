package my.code.crmservice.database.repository.deal;

import my.code.crmservice.database.entity.deal.Deal;
import my.code.crmservice.database.entity.deal.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {

    List<Deal> findAllByClientId(UUID clientId);

    Optional<Deal> findByIdAndClientId(UUID id, UUID clientId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Deal d SET d.status = :status, d.closedAt = :closedAt WHERE d.id = :id")
    void closeDeal(UUID id, DealStatus status, Instant closedAt);
}
