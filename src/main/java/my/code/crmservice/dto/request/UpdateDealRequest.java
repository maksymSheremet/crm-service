package my.code.crmservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Всі поля nullable — PATCH семантика.
// Статус НЕ тут — для зміни статусу є окремий endpoint PATCH /deals/{id}/close
public record UpdateDealRequest(

        @Size(max = 255)
        String title,

        @DecimalMin(value = "0.0", inclusive = false, message = "Value must be positive")
        BigDecimal value,

        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code (e.g. USD, EUR, UAH)")
        String currency

) {}
