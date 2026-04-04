ALTER TABLE usuarios
ADD COLUMN email_code VARCHAR(6),
ADD COLUMN email_code_expiracao TIMESTAMP,
ADD COLUMN tentativas_a2f INT DEFAULT 0 NOT NULL;