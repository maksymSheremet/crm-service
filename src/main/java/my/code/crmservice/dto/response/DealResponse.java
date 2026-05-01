package my.code.crmservice.dto.response;

import my.code.crmservice.database.entity.deal.DealStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DealResponse(
        UUID id,
        UUID clientId,
        String title,
        BigDecimal value,
        String currency,
        DealStatus status,
        Instant closedAt,   // null якщо OPEN
        Instant createdAt,
        Instant updatedAt
) {}
