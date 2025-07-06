-- Migração SIMPLES para alterar users.id de BIGSERIAL para UUID
-- V9__alter_user_id_to_uuid.sql
-- Para tabelas vazias: DELETE e CREATE

-- Garantir que a extensão UUID está habilitada
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Deletar tabelas se existirem (já que estão vazias)
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS users;

-- 2. Recriar tabela users com UUID
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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

-- 3. Recriar tabela user_roles com UUID
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. Recriar tabela refresh_tokens
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);

-- 5. Remover índices existentes e recriar
DROP INDEX IF EXISTS idx_users_email;
DROP INDEX IF EXISTS idx_users_google_id;
DROP INDEX IF EXISTS idx_users_active;
DROP INDEX IF EXISTS idx_roles_name;
DROP INDEX IF EXISTS idx_refresh_tokens_token;
DROP INDEX IF EXISTS idx_refresh_tokens_user_email;
DROP INDEX IF EXISTS idx_refresh_tokens_expiry;

-- Recriar índices
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_google_id ON users(google_id);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_email ON refresh_tokens(user_email);
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens(expiry_date);

-- 6. Recriar comentários
COMMENT ON TABLE users IS 'Tabela de usuários do sistema';
COMMENT ON TABLE user_roles IS 'Tabela de relacionamento entre usuários e roles';
COMMENT ON TABLE refresh_tokens IS 'Tabela de tokens de refresh para autenticação';

COMMENT ON COLUMN users.id IS 'ID único do usuário (UUID)';
COMMENT ON COLUMN users.google_id IS 'ID do usuário no Google OAuth';
COMMENT ON COLUMN users.subscription_plan IS 'Plano de assinatura do usuário';
COMMENT ON COLUMN refresh_tokens.expiry_date IS 'Data de expiração do token';