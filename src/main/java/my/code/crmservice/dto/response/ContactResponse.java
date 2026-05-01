package my.code.crmservice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ContactResponse(
        UUID id,
        UUID clientId,
        String name,
        String email,
        String phone,
        String role,
        Instant createdAt,
        Instant updatedAt
) {}
