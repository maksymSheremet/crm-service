-- ============================================================
-- V1__create_clients_table.sql
-- Головна таблиця CRM — ядро всього сервісу.
-- Кожен client належить конкретному user (userId = ownership).
-- ============================================================

-- Enum для lifecycle клієнта:
-- LEAD     — потенційний клієнт, ще не купував
-- ACTIVE   — активний, є угоди або контракт
-- INACTIVE — тимчасово неактивний (не churned, можна повернути)
-- CHURNED  — пішов назавжди
CREATE TYPE client_status AS ENUM ('LEAD', 'ACTIVE', 'INACTIVE', 'CHURNED');

CREATE TABLE clients (
    -- UUID генерується на рівні БД — globally unique, не вгадується в URL
                         id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Ownership: який user створив цього клієнта.
    -- BIGINT — відповідає типу PK в auth-service/user-service (BIGINT IDENTITY)
    -- X-User-Id хедер від gateway містить саме це значення
    -- NOT NULL + INDEX — кожен SELECT фільтрує по user_id першим
                         user_id     BIGINT      NOT NULL,

    -- Основні поля клієнта
                         name        VARCHAR(255) NOT NULL,
                         email       VARCHAR(255),
                         phone       VARCHAR(50),
                         company     VARCHAR(255),

    -- Lifecycle статус з enum — PostgreSQL валідує значення на рівні БД
                         status      client_status NOT NULL DEFAULT 'LEAD',

    -- Аудит: created_at НЕ NULL, updated_at може бути NULL до першого update
                         created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                         updated_at  TIMESTAMP
);

-- ── Індекси ─────────────────────────────────────────────────
-- Найважливіший індекс: кожен SELECT фільтрує по user_id
-- (SELECT * FROM clients WHERE user_id = ? AND ...)
CREATE INDEX idx_clients_user_id ON clients (user_id);

-- Складений індекс для фільтрації по user + status одночасно
-- (SELECT * FROM clients WHERE user_id = ? AND status = 'ACTIVE')
CREATE INDEX idx_clients_user_id_status ON clients (user_id, status);