package my.code.crmservice.exception;

// Один клас замість окремих ClientNotFoundException, DealNotFoundException тощо.
// HTTP 404 — GlobalExceptionHandler перетворить на відповідний response.
// Формат: "Client not found: 550e8400-e29b-41d4-a716-446655440000"
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " not found: " + id);
    }
}
