package my.code.crmservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Note — append-only: тільки створення, без update
public record CreateNoteRequest(

        @NotBlank(message = "Content is required")
        @Size(max = 10000, message = "Note content must not exceed 10000 characters")
        String content

) {}