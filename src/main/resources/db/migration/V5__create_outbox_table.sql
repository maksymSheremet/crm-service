-- ============================================================
--V5__create_outbox_table.sql
-- Transactional Outbox Pattern — надійна публікація Kafka events.
--
-- Проблема без outbox:
--   1. Зберігаємо client в БД  ✓
--   2. Публікуємо в Kafka      ✗ (сервіс впав між кроками)
--   → event загублено, billing не дізнався про нового клієнта
--
-- Рішення з outbox:
--   1. Зберігаємо client В ТІЙ ЖЕ транзакції що і outbox record ✓
--   2. Scheduler читає outbox і публікує в Kafka               ✓
--   3. Позначаємо record як PROCESSED                          ✓
--   → атомарність гарантована транзакцією PostgreSQL
-- ============================================================

-- Enum для статусу обробки outbox record
CREATE TYPE outbox_status AS ENUM ('PENDING', 'PROCESSED', 'FAILED');

CREATE TABLE outbox (
                        id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Тип події: CLIENT_CREATED, DEAL_CLOSED тощо
    -- Scheduler використовує це для вибору правильного Kafka topic
                        event_type      VARCHAR(100) NOT NULL,

    -- JSON payload події (серіалізований об'єкт)
    -- JSONB замість TEXT: PostgreSQL може індексувати, валідувати JSON
                        payload         JSONB        NOT NULL,

    -- Статус обробки: PENDING → PROCESSED або FAILED
                        status          outbox_status NOT NULL DEFAULT 'PENDING',

    -- Кількість спроб публікації (для exponential backoff або dead letter)
                        retry_count     INT          NOT NULL DEFAULT 0,

                        created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

    -- NULL поки не оброблено; встановлюється scheduler-ом після успішної публікації
                        processed_at    TIMESTAMP
);

-- ── Індекси ─────────────────────────────────────────────────
-- Найважливіший: scheduler робить цей запит кожні N секунд
-- SELECT * FROM outbox WHERE status = 'PENDING' ORDER BY created_at
-- Partial index — індексує ТІЛЬКИ PENDING записи (оброблені не потрібні)
CREATE INDEX idx_outbox_pending ON outbox (created_at)
    WHERE status = 'PENDING';