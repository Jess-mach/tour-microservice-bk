-- DDL PostgreSQL para TourApp Database
-- Criação das tabelas e relacionamentos

-- Extensão para UUID (caso não esteja habilitada)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela: users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    profile_picture VARCHAR(255),
    google_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    subscription_plan VARCHAR(50),
    subscription_expiry TIMESTAMP
);

-- Tabela: roles
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela de relacionamento: user_roles (Many-to-Many entre users e roles)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Tabela: tours
CREATE TABLE tours (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    duration_days INTEGER NOT NULL,
    max_participants INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tours_price CHECK (price > 0),
    CONSTRAINT chk_tours_duration CHECK (duration_days >= 1 AND duration_days <= 365),
    CONSTRAINT chk_tours_participants CHECK (max_participants >= 1 AND max_participants <= 100),
    CONSTRAINT chk_tours_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'CANCELLED', 'FULL'))
);

-- Tabela: refresh_tokens
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);

-- Índices para melhor performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_google_id ON users(google_id);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_tours_status ON tours(status);
CREATE INDEX idx_tours_destination ON tours(destination);
CREATE INDEX idx_tours_created_at ON tours(created_at);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_email ON refresh_tokens(user_email);
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens(expiry_date);

-- Função para atualizar automaticamente o campo updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger para atualizar automaticamente updated_at na tabela tours
CREATE TRIGGER update_tours_updated_at
    BEFORE UPDATE ON tours
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Inserção dos roles padrão
INSERT INTO roles (name) VALUES
    ('ROLE_USER'),
    ('ROLE_ADMIN'),
    ('ROLE_PREMIUM')
ON CONFLICT (name) DO NOTHING;

-- Comentários nas tabelas
COMMENT ON TABLE users IS 'Tabela de usuários do sistema';
COMMENT ON TABLE roles IS 'Tabela de perfis/funções dos usuários';
COMMENT ON TABLE user_roles IS 'Tabela de relacionamento entre usuários e roles';
COMMENT ON TABLE tours IS 'Tabela de tours/pacotes turísticos';
COMMENT ON TABLE refresh_tokens IS 'Tabela de tokens de refresh para autenticação';

-- Comentários nas colunas principais
COMMENT ON COLUMN users.google_id IS 'ID do usuário no Google OAuth';
COMMENT ON COLUMN users.subscription_plan IS 'Plano de assinatura do usuário';
COMMENT ON COLUMN tours.duration_days IS 'Duração do tour em dias';
COMMENT ON COLUMN tours.max_participants IS 'Número máximo de participantes';
COMMENT ON COLUMN tours.status IS 'Status do tour: ACTIVE, INACTIVE, CANCELLED, FULL';
COMMENT ON COLUMN refresh_tokens.expiry_date IS 'Data de expiração do token';