package my.code.crmservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateDealRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 255)
        String title,

        // Nullable: угоду можна створити без суми (не завжди відома на старті)
        @DecimalMin(value = "0.0", inclusive = false, message = "Value must be positive")
        BigDecimal value,

        // ISO 4217: рівно 3 великі літери (USD, EUR, UAH)
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code (e.g. USD, EUR, UAH)")
        String currency

) {}
