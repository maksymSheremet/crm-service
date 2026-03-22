-- ============================================================
-- V4__create_deals_table.sql
-- Угоди (deals) в межах клієнта (1 client → N deals).
-- Deal має власний lifecycle: OPEN → WON або LOST.
-- Закриття deal тригерить Kafka event DEAL_CLOSED.
-- ============================================================

-- Enum для lifecycle угоди
-- OPEN  — угода в роботі
-- WON   — угода виграна (клієнт купив)
-- LOST  — угода програна (клієнт відмовив)
CREATE TYPE deal_status AS ENUM ('OPEN', 'WON', 'LOST');

CREATE TABLE deals (
                       id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    -- FK на clients: угода завжди прив'язана до клієнта
    -- RESTRICT (замість CASCADE): не дозволяємо видаляти клієнта поки є угоди
    -- Це захист від випадкового DELETE — спочатку закрий або перенеси угоди
                       client_id   UUID            NOT NULL REFERENCES clients (id) ON DELETE RESTRICT,

    -- Назва угоди: "Annual License 2025", "Enterprise Contract Q3" тощо
                       title       VARCHAR(255)    NOT NULL,

    -- Фінансова цінність угоди
    -- NUMERIC(15,2): до 999 млрд з точністю до копійки
                       value       NUMERIC(15, 2),

    -- Валюта у форматі ISO 4217: USD, EUR, UAH
    -- DEFAULT 'USD' — найпоширеніший варіант
                       currency    VARCHAR(3)      NOT NULL DEFAULT 'USD',

    -- Lifecycle статус
                       status      deal_status     NOT NULL DEFAULT 'OPEN',

    -- NULL поки угода OPEN, встановлюється при PATCH /deals/{id}/close
    -- Не використовуємо updated_at для цього — closed_at є явним бізнес-фактом
                       closed_at   TIMESTAMP,

                       created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMP
);

-- ── Індекси ─────────────────────────────────────────────────
-- Основний доступ: всі угоди по клієнту
CREATE INDEX idx_deals_client_id ON deals (client_id);

-- Фільтрація по статусу: "покажи всі відкриті угоди клієнта"
CREATE INDEX idx_deals_client_id_status ON deals (client_id, status);