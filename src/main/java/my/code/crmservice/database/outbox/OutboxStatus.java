package my.code.crmservice.database.outbox;

public enum OutboxStatus {
    PENDING,
    PROCESSED,
    FAILED
}
