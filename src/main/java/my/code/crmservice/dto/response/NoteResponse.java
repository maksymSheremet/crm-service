package my.code.crmservice.dto.response;

import java.time.Instant;
import java.util.UUID;

// Без updatedAt — Note є append-only, редагування не передбачено
public record NoteResponse(
        UUID id,
        UUID clientId,
        Long authorId,
        String content,
        Instant createdAt
) {}
