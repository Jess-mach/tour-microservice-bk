-- ===========================================
-- V14__create_new_structure_clean.sql
-- Criar estrutura limpa (dropar e recriar se necessário)
-- ===========================================

-- PASSO 1: Criar função para updated_at se não existir
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- PASSO 2: Corrigir tabela roles
-- Adicionar coluna description se não existir
ALTER TABLE roles ADD COLUMN IF NOT EXISTS description VARCHAR(255);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Garantir constraints
ALTER TABLE roles ALTER COLUMN name SET NOT NULL;
DO $$
BEGIN
    ALTER TABLE roles ADD CONSTRAINT uk_roles_name UNIQUE (name);
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;

-- Trigger para roles
DROP TRIGGER IF EXISTS update_roles_updated_at ON roles;
CREATE TRIGGER update_roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- PASSO 3: Dropar e recriar tabela companias
DROP TABLE IF EXISTS user_compania CASCADE;
DROP TABLE IF EXISTS companias CASCADE;

CREATE TABLE companias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    google_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    -- Dados da empresa
    nome_empresa VARCHAR(150) NOT NULL,
    cnpj VARCHAR(18) UNIQUE,
    descricao TEXT,
    site VARCHAR(200),
    logo_url TEXT,

    -- Dados bancários
    pix_key VARCHAR(100),

    -- Endereço
    cep VARCHAR(10),
    endereco VARCHAR(200),
    cidade VARCHAR(100),
    estado VARCHAR(2),

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',

    CONSTRAINT chk_companias_status CHECK (status IN ('ATIVA', 'SUSPENSA', 'INATIVA', 'PENDENTE_APROVACAO')),
    CONSTRAINT chk_companias_cnpj_format CHECK (cnpj IS NULL OR LENGTH(cnpj) >= 14)
);

-- PASSO 4: Criar tabela user_compania
CREATE TABLE user_compania (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    google_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    -- Relacionamentos
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    compania_id UUID NOT NULL REFERENCES companias(id) ON DELETE CASCADE,

    -- Role e permissões
    role_compania VARCHAR(20) NOT NULL,
    data_ingresso TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Permissões granulares
    pode_criar_excursoes BOOLEAN NOT NULL DEFAULT true,
    pode_gerenciar_usuarios BOOLEAN NOT NULL DEFAULT false,
    pode_ver_financeiro BOOLEAN NOT NULL DEFAULT true,
    pode_editar_compania BOOLEAN NOT NULL DEFAULT false,
    pode_enviar_notificacoes BOOLEAN NOT NULL DEFAULT true,

    -- Dados do convite
    convidado_por VARCHAR(150),
    data_aceite_convite TIMESTAMP,
    observacoes VARCHAR(500),

    CONSTRAINT uk_user_compania UNIQUE(user_id, compania_id),
    CONSTRAINT chk_user_compania_role CHECK (role_compania IN ('ADMIN', 'ORGANIZADOR', 'COLABORADOR')),
    CONSTRAINT chk_user_compania_data_aceite CHECK (data_aceite_convite IS NULL OR data_aceite_convite >= data_ingresso)
);

-- PASSO 5: Atualizar tabela users
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS cep VARCHAR(10);
ALTER TABLE users ADD COLUMN IF NOT EXISTS endereco VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS cidade VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS estado VARCHAR(2);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_notifications BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS sms_notifications BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS push_token TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Garantir constraints obrigatórias
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN full_name SET NOT NULL;
ALTER TABLE users ALTER COLUMN active SET NOT NULL;
ALTER TABLE users ALTER COLUMN active SET DEFAULT true;

-- PASSO 6: Criar índices
CREATE INDEX idx_companias_cnpj ON companias(cnpj);
CREATE INDEX idx_companias_status ON companias(status);
CREATE INDEX idx_companias_cidade_estado ON companias(cidade, estado);
CREATE INDEX idx_companias_nome_empresa ON companias(nome_empresa);

CREATE INDEX idx_user_compania_user_id ON user_compania(user_id);
CREATE INDEX idx_user_compania_compania_id ON user_compania(compania_id);
CREATE INDEX idx_user_compania_role ON user_compania(role_compania);
CREATE INDEX idx_user_compania_ativo ON user_compania(ativo);
CREATE INDEX idx_user_compania_data_ingresso ON user_compania(data_ingresso);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_users_cidade_estado ON users(cidade, estado);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- PASSO 7: Criar triggers
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_companias_updated_at ON companias;
CREATE TRIGGER update_companias_updated_at
    BEFORE UPDATE ON companias
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_user_compania_updated_at ON user_compania;
CREATE TRIGGER update_user_compania_updated_at
    BEFORE UPDATE ON user_compania
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();