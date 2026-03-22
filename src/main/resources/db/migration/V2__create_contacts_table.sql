-- ============================================================
-- V2__create_contacts_table.sql
-- Контактні особи в межах клієнта (1 client → N contacts).
-- Contact не має прямого user_id — ownership перевіряється
-- через client (JOIN clients WHERE clients.user_id = ?).
-- ============================================================

CREATE TABLE contacts (
                          id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- FK на clients: при видаленні клієнта всі контакти видаляються автоматично
    -- CASCADE — логічно: немає клієнта = немає сенсу тримати його контакти
                          client_id   UUID        NOT NULL REFERENCES clients (id) ON DELETE CASCADE,

    -- Контактна особа може мати ім'я, email, телефон і роль в компанії
    -- Всі поля nullable: можемо зберегти контакт з мінімумом даних
                          name        VARCHAR(255) NOT NULL,
                          email       VARCHAR(255),
                          phone       VARCHAR(50),

    -- Роль в компанії клієнта: "CEO", "CTO", "Procurement Manager" тощо
                          role        VARCHAR(100),

                          created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at  TIMESTAMP
);

-- ── Індекси ─────────────────────────────────────────────────
-- Всі запити до contacts ідуть через client_id
-- (SELECT * FROM contacts WHERE client_id = ?)
CREATE INDEX idx_contacts_client_id ON contacts (client_id);