package my.code.crmservice.dto.response;

import my.code.crmservice.database.entity.client.ClientStatus;

import java.time.Instant;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        Long userId,
        String name,
        String email,
        String phone,
        String company,
        ClientStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
