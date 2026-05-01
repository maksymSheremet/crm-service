package my.code.crmservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

// Всі поля nullable — PATCH семантика (як UpdateClientRequest)
public record UpdateContactRequest(

        @Size(max = 255)
        String name,

        @Email(message = "Email must be valid")
        @Size(max = 255)
        String email,

        @Size(max = 50)
        String phone,

        @Size(max = 100)
        String role

) {}
