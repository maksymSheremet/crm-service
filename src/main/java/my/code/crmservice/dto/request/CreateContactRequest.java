package my.code.crmservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateContactRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 255)
        String name,

        @Email(message = "Email must be valid")
        @Size(max = 255)
        String email,

        @Size(max = 50)
        String phone,

        // Роль в компанії: "CEO", "CTO", "Procurement Manager" тощо
        @Size(max = 100)
        String role

) {}
