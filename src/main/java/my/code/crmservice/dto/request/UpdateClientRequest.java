package my.code.crmservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import my.code.crmservice.database.entity.client.ClientStatus;

// Всі поля nullable — PATCH семантика.
// MapStruct з IGNORE стратегією не перезапише поля entity якщо поле тут null.
// Приклад: { "phone": "+380501234567" } — оновить тільки phone, решта без змін.
public record UpdateClientRequest(

        @Size(max = 255)
        String name,

        @Email(message = "Email must be valid")
        @Size(max = 255)
        String email,

        @Size(max = 50)
        String phone,

        @Size(max = 255)
        String company,

        // Дозволяємо змінювати статус через update (LEAD → ACTIVE тощо)
        ClientStatus status

) {}
