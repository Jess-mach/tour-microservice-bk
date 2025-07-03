-- V8__add_missing_fields.sql
-- Migration para adicionar campos que estão faltando nas entidades

-- Adicionar campos de endereço na tabela clientes
ALTER TABLE clientes
ADD COLUMN IF NOT EXISTS cep VARCHAR(10),
ADD COLUMN IF NOT EXISTS endereco VARCHAR(200),
ADD COLUMN IF NOT EXISTS cidade VARCHAR(100),
ADD COLUMN IF NOT EXISTS estado VARCHAR(2),
ADD COLUMN IF NOT EXISTS google_id VARCHAR(255) UNIQUE;

-- Adicionar campos faltantes na tabela organizadores
ALTER TABLE organizadores
ADD COLUMN IF NOT EXISTS nome VARCHAR(100),
ADD COLUMN IF NOT EXISTS cep VARCHAR(10),
ADD COLUMN IF NOT EXISTS endereco VARCHAR(200),
ADD COLUMN IF NOT EXISTS cidade VARCHAR(100),
ADD COLUMN IF NOT EXISTS estado VARCHAR(2),
ADD COLUMN IF NOT EXISTS descricao VARCHAR(1000),
ADD COLUMN IF NOT EXISTS site VARCHAR(200),
ADD COLUMN IF NOT EXISTS google_id VARCHAR(255) UNIQUE;

-- Adicionar constraint única no CNPJ se não existir
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'uk_organizadores_cnpj'
        AND table_name = 'organizadores'
    ) THEN
        ALTER TABLE organizadores ADD CONSTRAINT uk_organizadores_cnpj UNIQUE (cnpj);
    END IF;
END $$;

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_clientes_google_id ON clientes(google_id);
CREATE INDEX IF NOT EXISTS idx_organizadores_google_id ON organizadores(google_id);
CREATE INDEX IF NOT EXISTS idx_clientes_email ON clientes(email);
CREATE INDEX IF NOT EXISTS idx_organizadores_email ON organizadores(email);