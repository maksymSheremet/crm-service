-- ============================================================
-- V3__create_notes_table.sql
-- Нотатки по клієнту (1 client → N notes).
-- На відміну від contacts, note має author_id —
-- знаємо хто написав (важливо для аудиту в team-акаунтах).
-- ============================================================

CREATE TABLE notes (
                       id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- FK на clients з CASCADE: клієнт видалений → всі нотатки видалені
                       client_id   UUID        NOT NULL REFERENCES clients (id) ON DELETE CASCADE,

    -- Хто написав нотатку (аудит).
    -- BIGINT — відповідає типу PK users в auth/user-service
    -- Значення береться з X-User-Id хедера при створенні
    -- NOT NULL: нотатка без автора не має сенсу
                       author_id   BIGINT      NOT NULL,

    -- TEXT (не VARCHAR) — нотатки можуть бути довільної довжини
                       content     TEXT        NOT NULL,

    -- Тільки created_at: нотатки не редагуються (append-only за природою)
                       created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── Індекси ─────────────────────────────────────────────────
-- Основний доступ: всі нотатки по клієнту
CREATE INDEX idx_notes_client_id ON notes (client_id);