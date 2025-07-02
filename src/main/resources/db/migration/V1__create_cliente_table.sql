CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    push_token VARCHAR(255),
    email_notifications BOOLEAN NOT NULL DEFAULT true,
    sms_notifications BOOLEAN NOT NULL DEFAULT true,
    ativo BOOLEAN NOT NULL DEFAULT true,
    tipo_usuario VARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_clientes_email ON clientes(email);
CREATE INDEX idx_clientes_ativo ON clientes(ativo);

