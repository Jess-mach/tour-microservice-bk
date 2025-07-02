CREATE TABLE organizadores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome_empresa VARCHAR(150) NOT NULL,
    nome_responsavel VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    pix_key VARCHAR(100),
    cnpj VARCHAR(18),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    tipo_usuario VARCHAR(20) NOT NULL DEFAULT 'ORGANIZADOR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_organizadores_email ON organizadores(email);
CREATE INDEX idx_organizadores_status ON organizadores(status);

