-- Inserir dados iniciais para testes (opcional)

-- Organizador de teste
INSERT INTO organizadores (
    id, nome_empresa, nome_responsavel, email, senha, telefone, pix_key, status
) VALUES (
    uuid_generate_v4(),
    'Turismo Aventura',
    'João Silva',
    'joao@turismoaventura.com',
    '$2a$10$N.zmdr9k7uH7NOARz4dBx.N5K9fxDn.lKNa6XMTtmCF5ZEaQBj2IW', -- senha: 123456
    '(11) 99999-9999',
    'joao@turismoaventura.com',
    'ATIVO'
);

-- Cliente de teste
INSERT INTO clientes (
    id, nome, email, senha, telefone
) VALUES (
    uuid_generate_v4(),
    'Maria Santos',
    'maria@email.com',
    '$2a$10$N.zmdr9k7uH7NOARz4dBx.N5K9fxDn.lKNa6XMTtmCF5ZEaQBj2IW', -- senha: 123456
    '(11) 88888-8888'
);

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$ language 'plpgsql';

CREATE TRIGGER update_clientes_updated_at BEFORE UPDATE ON clientes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_organizadores_updated_at BEFORE UPDATE ON organizadores
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_excursoes_updated_at BEFORE UPDATE ON excursoes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_inscricoes_updated_at BEFORE UPDATE ON inscricoes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pagamentos_updated_at BEFORE UPDATE ON pagamentos
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notificacoes_updated_at BEFORE UPDATE ON notificacoes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

