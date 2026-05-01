package my.code.crmservice.dto.request;

import jakarta.validation.constraints.NotNull;
import my.code.crmservice.database.entity.deal.DealStatus;

// Окремий DTO для PATCH /deals/{id}/close — явна бізнес-команда.
// Статус може бути тільки WON або LOST (не OPEN).
// Валідація що статус != OPEN — на рівні сервісу (бізнес-правило, не Bean Validation)
public record CloseDealRequest(

        @NotNull(message = "Status is required")
        DealStatus status

) {}
