-- ===========================================
-- V8__create_companias_table.sql
-- ===========================================
-- Criar tabela de companias

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
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA'
);

-- Índices para performance
CREATE INDEX idx_companias_cnpj ON companias(cnpj);
CREATE INDEX idx_companias_status ON companias(status);
CREATE INDEX idx_companias_cidade_estado ON companias(cidade, estado);
CREATE INDEX idx_companias_nome_empresa ON companias(nome_empresa);

-- ===========================================
-- V9__create_user_compania_table.sql
-- ===========================================
-- Criar tabela de relacionamento user-compania

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

    -- Constraint de relacionamento único
    UNIQUE(user_id, compania_id)
);

-- Índices
CREATE INDEX idx_user_compania_user_id ON user_compania(user_id);
CREATE INDEX idx_user_compania_compania_id ON user_compania(compania_id);
CREATE INDEX idx_user_compania_role ON user_compania(role_compania);
CREATE INDEX idx_user_compania_ativo ON user_compania(ativo);
CREATE INDEX idx_user_compania_data_ingresso ON user_compania(data_ingresso);

-- ===========================================
-- V10__update_users_table.sql
-- ===========================================
-- Atualizar tabela users para ser mais completa

-- Adicionar campos que podem estar faltando
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS cep VARCHAR(10);
ALTER TABLE users ADD COLUMN IF NOT EXISTS endereco VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS cidade VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS estado VARCHAR(2);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_notifications BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS sms_notifications BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS push_token TEXT;

-- Atualizar campos existentes se necessário
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN full_name SET NOT NULL;
ALTER TABLE users ALTER COLUMN active SET NOT NULL;
ALTER TABLE users ALTER COLUMN active SET DEFAULT true;

-- Garantir que created_at existe
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Índices na tabela users
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_users_cidade_estado ON users(cidade, estado);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- ===========================================
-- V11__migrate_existing_data.sql
-- ===========================================
-- Migrar dados existentes das tabelas antigas

-- PASSO 1: Migrar dados dos Organizadores para Users
-- (Apenas se não existirem na tabela users)
INSERT INTO users (
    id, email, full_name, phone, cep, endereco, cidade, estado,
    email_notifications, sms_notifications, active, created_at, updated_at, google_id
)
SELECT
    o.id,
    o.email,
    COALESCE(o.nome, o.nome_responsavel, o.email),
    o.telefone,
    o.cep,
    o.endereco,
    o.cidade,
    o.estado,
    true,
    true,
    CASE WHEN o.status = 'ATIVO' THEN true ELSE false END,
    COALESCE(o.created_at, CURRENT_TIMESTAMP),
    COALESCE(o.updated_at, CURRENT_TIMESTAMP),
    o.google_id
FROM organizadores o
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = o.email);

-- PASSO 2: Migrar dados dos Clientes para Users
-- (Apenas se não existirem na tabela users)
INSERT INTO users (
    id, email, full_name, phone, cep, endereco, cidade, estado,
    email_notifications, sms_notifications, push_token, active, created_at, updated_at, google_id
)
SELECT
    c.id,
    c.email,
    c.nome,
    c.telefone,
    c.cep,
    c.endereco,
    c.cidade,
    c.estado,
    COALESCE(c.email_notifications, true),
    COALESCE(c.sms_notifications, true),
    c.push_token,
    COALESCE(c.ativo, true),
    COALESCE(c.created_at, CURRENT_TIMESTAMP),
    COALESCE(c.updated_at, CURRENT_TIMESTAMP),
    c.google_id
FROM clientes c
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = c.email);

-- PASSO 3: Criar Companias baseadas nos Organizadores
INSERT INTO companias (
    nome_empresa, cnpj, descricao, site, pix_key,
    cep, endereco, cidade, estado, status, created_at, updated_at
)
SELECT DISTINCT
    o.nome_empresa,
    o.cnpj,
    o.descricao,
    o.site,
    o.pix_key,
    o.cep,
    o.endereco,
    o.cidade,
    o.estado,
    CASE
        WHEN o.status = 'ATIVO' THEN 'ATIVA'
        WHEN o.status = 'PENDENTE' THEN 'PENDENTE_APROVACAO'
        ELSE 'INATIVA'
    END,
    COALESCE(o.created_at, CURRENT_TIMESTAMP),
    COALESCE(o.updated_at, CURRENT_TIMESTAMP)
FROM organizadores o
WHERE o.nome_empresa IS NOT NULL;

-- PASSO 4: Criar relacionamentos User-Compania
-- Relacionar cada organizador com sua compania como ADMIN
INSERT INTO user_compania (
    user_id, compania_id, role_compania, data_ingresso, ativo,
    pode_criar_excursoes, pode_gerenciar_usuarios, pode_ver_financeiro,
    pode_editar_compania, pode_enviar_notificacoes
)
SELECT DISTINCT
    u.id,
    c.id,
    'ADMIN',
    COALESCE(o.created_at, CURRENT_TIMESTAMP),
    CASE WHEN o.status = 'ATIVO' THEN true ELSE false END,
    true, true, true, true, true
FROM organizadores o
JOIN users u ON u.email = o.email
JOIN companias c ON c.nome_empresa = o.nome_empresa
    AND (c.cnpj = o.cnpj OR (c.cnpj IS NULL AND o.cnpj IS NULL))
WHERE o.nome_empresa IS NOT NULL;

-- ===========================================
-- V12__update_table_references.sql
-- ===========================================
-- Atualizar referências nas tabelas existentes

-- PASSO 1: Adicionar novas colunas nas tabelas
ALTER TABLE excursoes ADD COLUMN compania_id UUID;
ALTER TABLE excursoes ADD COLUMN criador_id UUID;
ALTER TABLE inscricoes ADD COLUMN user_id UUID;
ALTER TABLE notificacoes ADD COLUMN compania_id UUID;
ALTER TABLE notificacoes ADD COLUMN criador_id UUID;

-- PASSO 2: Popular as novas colunas
-- Atualizar excursões
UPDATE excursoes SET
    compania_id = (
        SELECT c.id
        FROM companias c
        JOIN user_compania uc ON c.id = uc.compania_id
        JOIN users u ON uc.user_id = u.id
        WHERE u.email = (SELECT email FROM organizadores WHERE id = excursoes.organizador_id)
        LIMIT 1
    ),
    criador_id = (
        SELECT u.id
        FROM users u
        WHERE u.email = (SELECT email FROM organizadores WHERE id = excursoes.organizador_id)
        LIMIT 1
    )
WHERE organizador_id IS NOT NULL;

-- Atualizar inscrições
UPDATE inscricoes SET
    user_id = (
        SELECT u.id
        FROM users u
        WHERE u.email = (SELECT email FROM clientes WHERE id = inscricoes.cliente_id)
        LIMIT 1
    )
WHERE cliente_id IS NOT NULL;

-- Atualizar notificações
UPDATE notificacoes SET
    compania_id = (
        SELECT c.id
        FROM companias c
        JOIN user_compania uc ON c.id = uc.compania_id
        JOIN users u ON uc.user_id = u.id
        WHERE u.email = (SELECT email FROM organizadores WHERE id = notificacoes.organizador_id)
        LIMIT 1
    ),
    criador_id = (
        SELECT u.id
        FROM users u
        WHERE u.email = (SELECT email FROM organizadores WHERE id = notificacoes.organizador_id)
        LIMIT 1
    )
WHERE organizador_id IS NOT NULL;

-- PASSO 3: Adicionar constraints das novas colunas
ALTER TABLE excursoes ADD CONSTRAINT fk_excursoes_compania
    FOREIGN KEY (compania_id) REFERENCES companias(id);
ALTER TABLE excursoes ADD CONSTRAINT fk_excursoes_criador
    FOREIGN KEY (criador_id) REFERENCES users(id);
ALTER TABLE inscricoes ADD CONSTRAINT fk_inscricoes_user
    FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE notificacoes ADD CONSTRAINT fk_notificacoes_compania
    FOREIGN KEY (compania_id) REFERENCES companias(id);
ALTER TABLE notificacoes ADD CONSTRAINT fk_notificacoes_criador
    FOREIGN KEY (criador_id) REFERENCES users(id);

-- PASSO 4: Tornar as colunas obrigatórias (após popular)
ALTER TABLE excursoes ALTER COLUMN compania_id SET NOT NULL;
ALTER TABLE excursoes ALTER COLUMN criador_id SET NOT NULL;
ALTER TABLE inscricoes ALTER COLUMN user_id SET NOT NULL;

-- Índices para as novas colunas
CREATE INDEX idx_excursoes_compania_id ON excursoes(compania_id);
CREATE INDEX idx_excursoes_criador_id ON excursoes(criador_id);
CREATE INDEX idx_inscricoes_user_id ON inscricoes(user_id);
CREATE INDEX idx_notificacoes_compania_id ON notificacoes(compania_id);
CREATE INDEX idx_notificacoes_criador_id ON notificacoes(criador_id);

-- ===========================================
-- V13__cleanup_old_columns.sql
-- ===========================================
-- Remover colunas antigas após validação

-- ATENÇÃO: Execute apenas após validar que os dados foram migrados corretamente!

-- Remover constraints das colunas antigas
ALTER TABLE excursoes DROP CONSTRAINT IF EXISTS fk_excursoes_organizador;
ALTER TABLE inscricoes DROP CONSTRAINT IF EXISTS fk_inscricoes_cliente;
ALTER TABLE notificacoes DROP CONSTRAINT IF EXISTS fk_notificacoes_organizador;

-- Remover colunas antigas
ALTER TABLE excursoes DROP COLUMN IF EXISTS organizador_id;
ALTER TABLE inscricoes DROP COLUMN IF EXISTS cliente_id;
ALTER TABLE notificacoes DROP COLUMN IF EXISTS organizador_id;

-- ===========================================
-- V14__add_validation_constraints.sql
-- ===========================================
-- Adicionar constraints de validação e integridade

-- Constraints para Companias
ALTER TABLE companias ADD CONSTRAINT chk_companias_status
    CHECK (status IN ('ATIVA', 'SUSPENSA', 'INATIVA', 'PENDENTE_APROVACAO'));

ALTER TABLE companias ADD CONSTRAINT chk_companias_cnpj_format
    CHECK (cnpj IS NULL OR LENGTH(cnpj) >= 14);

-- Constraints para UserCompania
ALTER TABLE user_compania ADD CONSTRAINT chk_user_compania_role
    CHECK (role_compania IN ('ADMIN', 'ORGANIZADOR', 'COLABORADOR'));

ALTER TABLE user_compania ADD CONSTRAINT chk_user_compania_data_aceite
    CHECK (data_aceite_convite IS NULL OR data_aceite_convite >= data_ingresso);

-- Garantir que toda compania tem pelo menos um admin ativo
-- (Implementado via trigger ou validação na aplicação)

-- ===========================================
-- V15__create_audit_triggers.sql
-- ===========================================
-- Criar triggers para auditoria e manutenção

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$ language 'plpgsql';

-- Aplicar trigger nas tabelas principais
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_companias_updated_at
    BEFORE UPDATE ON companias
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_compania_updated_at
    BEFORE UPDATE ON user_compania
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ===========================================
-- V16__insert_default_data.sql
-- ===========================================
-- Inserir dados padrão se necessário

-- Verificar se roles existem, se não, criar
INSERT INTO roles (name, description) VALUES
    ('ROLE_USER', 'Usuário básico')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('ROLE_CLIENTE', 'Cliente que faz inscrições')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('ROLE_ORGANIZADOR', 'Organizador de excursões')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN', 'Administrador do sistema')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, description) VALUES
    ('ROLE_PREMIUM', 'Usuário premium')
ON CONFLICT (name) DO NOTHING;

-- Garantir que todos os usuários tenham pelo menos ROLE_USER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE r.name = 'ROLE_USER'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- Adicionar ROLE_CLIENTE para usuários que têm inscrições
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT u.id, r.id
FROM users u
JOIN inscricoes i ON i.user_id = u.id
CROSS JOIN roles r
WHERE r.name = 'ROLE_CLIENTE'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- Adicionar ROLE_ORGANIZADOR para usuários que fazem parte de companias
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT u.id, r.id
FROM users u
JOIN user_compania uc ON uc.user_id = u.id
CROSS JOIN roles r
WHERE r.name = 'ROLE_ORGANIZADOR'
AND uc.ativo = true
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- ===========================================
-- V17__create_views_for_compatibility.sql
-- ===========================================
-- Criar views para compatibilidade com código existente

-- View que simula a antiga tabela clientes
CREATE OR REPLACE VIEW clientes AS
SELECT
    u.id,
    u.google_id,
    u.created_at,
    u.updated_at,
    u.version,
    u.full_name as nome,
    u.email,
    'migrated' as senha, -- Placeholder para compatibilidade
    u.phone as telefone,
    u.cep,
    u.endereco,
    u.cidade,
    u.estado,
    u.push_token,
    u.email_notifications,
    u.sms_notifications,
    u.active as ativo,
    'CLIENTE' as tipo_usuario
FROM users u
WHERE EXISTS (
    SELECT 1 FROM user_roles ur
    JOIN roles r ON ur.role_id = r.id
    WHERE ur.user_id = u.id AND r.name = 'ROLE_CLIENTE'
);

-- View que simula a antiga tabela organizadores
CREATE OR REPLACE VIEW organizadores AS
SELECT
    u.id,
    u.google_id,
    u.created_at,
    u.updated_at,
    u.version,
    c.nome_empresa,
    u.full_name as nome_responsavel,
    u.full_name as nome,
    u.email,
    'migrated' as senha, -- Placeholder para compatibilidade
    u.phone as telefone,
    u.cep,
    u.endereco,
    u.cidade,
    u.estado,
    c.pix_key,
    c.cnpj,
    c.descricao,
    c.site,
    CASE
        WHEN c.status = 'ATIVA' THEN 'ATIVO'
        WHEN c.status = 'PENDENTE_APROVACAO' THEN 'PENDENTE'
        ELSE 'INATIVO'
    END as status,
    'ORGANIZADOR' as tipo_usuario
FROM users u
JOIN user_compania uc ON uc.user_id = u.id AND uc.ativo = true
JOIN companias c ON c.id = uc.compania_id
WHERE uc.role_compania = 'ADMIN'; -- Apenas admins aparecem como "organizadores principais"

-- ===========================================
-- V18__performance_optimization.sql
-- ===========================================
-- Otimizações de performance

-- Índices compostos para consultas frequentes
CREATE INDEX idx_user_compania_user_compania_ativo ON user_compania(user_id, compania_id, ativo);
CREATE INDEX idx_user_compania_compania_role_ativo ON user_compania(compania_id, role_compania, ativo);

-- Índices para consultas de excursões
CREATE INDEX idx_excursoes_compania_status ON excursoes(compania_id, status);
CREATE INDEX idx_excursoes_compania_data_saida ON excursoes(compania_id, data_saida);

-- Índices para consultas de inscrições
CREATE INDEX idx_inscricoes_user_excursao ON inscricoes(user_id, excursao_id);
CREATE INDEX idx_inscricoes_status_pagamento ON inscricoes(status_pagamento);

-- Índices para notificações
CREATE INDEX idx_notificacoes_compania_enviada ON notificacoes(compania_id, enviada);

-- Índices para users
CREATE INDEX idx_users_active_created_at ON users(active, created_at);
CREATE INDEX idx_users_email_notifications ON users(email_notifications) WHERE email_notifications = true;
CREATE INDEX idx_users_push_token ON users(push_token) WHERE push_token IS NOT NULL;

-- ===========================================
-- SCRIPT DE VALIDAÇÃO (Para executar após as migrations)
-- ===========================================

-- Validar integridade dos dados migrados
DO $
DECLARE
    total_organizadores INTEGER;
    total_companias INTEGER;
    total_user_compania INTEGER;
    total_clientes INTEGER;
    total_users_clientes INTEGER;
BEGIN
    -- Contar registros
    SELECT COUNT(*) INTO total_organizadores FROM organizadores;
    SELECT COUNT(*) INTO total_companias FROM companias;
    SELECT COUNT(*) INTO total_user_compania FROM user_compania;

    RAISE NOTICE 'Organizadores migrados: %', total_organizadores;
    RAISE NOTICE 'Companias criadas: %', total_companias;
    RAISE NOTICE 'Relacionamentos user-compania: %', total_user_compania;

    -- Verificar se todos os organizadores têm compania
    IF total_organizadores > total_user_compania THEN
        RAISE WARNING 'Alguns organizadores podem não ter sido migrados corretamente!';
    END IF;

    -- Verificar clientes
    SELECT COUNT(*) FROM clientes INTO total_clientes;
    SELECT COUNT(*) FROM users u
    JOIN user_roles ur ON ur.user_id = u.id
    JOIN roles r ON r.id = ur.role_id
    WHERE r.name = 'ROLE_CLIENTE' INTO total_users_clientes;

    RAISE NOTICE 'Clientes migrados: %', total_clientes;
    RAISE NOTICE 'Users com ROLE_CLIENTE: %', total_users_clientes;

    -- Verificar excursões
    IF EXISTS (SELECT 1 FROM excursoes WHERE compania_id IS NULL) THEN
        RAISE WARNING 'Existem excursões sem compania associada!';
    END IF;

    -- Verificar inscrições
    IF EXISTS (SELECT 1 FROM inscricoes WHERE user_id IS NULL) THEN
        RAISE WARNING 'Existem inscrições sem usuário associado!';
    END IF;

    RAISE NOTICE 'Validação concluída!';
END $;