package my.code.crmservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

// @RestControllerAdvice: перехоплює exceptions з усіх @RestController класів.
// ProblemDetail — стандарт RFC 7807 (Spring 6+): структурований JSON для помилок.
// Формат: { "type", "title", "status", "detail", "instance" }
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: Client/Contact/Deal не знайдено або не належить цьому user
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        log.debug("Resource not found: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400: Порушення бізнес-правила (закрити вже закриту угоду тощо)
    @ExceptionHandler(InvalidOperationException.class)
    public ProblemDetail handleInvalidOperation(InvalidOperationException ex) {
        log.debug("Invalid operation: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400: Bean Validation (@Valid) провалилась
    // Повертаємо map полів → повідомлення про помилку
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "Invalid value",
                        // merge function: якщо два errors на одне поле — беремо перше
                        (first, second) -> first
                ));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed");
        // Додаємо кастомну властивість з деталями полів
        problem.setProperty("errors", errors);
        return problem;
    }

    // 500: Будь-яка неочікувана помилка — логуємо повний стектрейс
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}