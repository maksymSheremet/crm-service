package my.code.crmservice.exception;

// Бізнес-правило порушено: HTTP 400 (не 404 і не 500).
// Приклад: спроба закрити вже закриту угоду, статус OPEN при close тощо.
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
