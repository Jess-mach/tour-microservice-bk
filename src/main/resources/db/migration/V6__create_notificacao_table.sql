CREATE TABLE notificacoes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organizador_id UUID NOT NULL REFERENCES organizadores(id) ON DELETE CASCADE,
    excursao_id UUID REFERENCES excursoes(id) ON DELETE CASCADE,
    titulo VARCHAR(100) NOT NULL,
    mensagem TEXT NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'INFO',
    enviada_em TIMESTAMP,
    enviar_para_todos BOOLEAN NOT NULL DEFAULT false,
    enviada BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_notificacoes_organizador ON notificacoes(organizador_id);
CREATE INDEX idx_notificacoes_excursao ON notificacoes(excursao_id);
CREATE INDEX idx_notificacoes_enviada ON notificacoes(enviada);
CREATE INDEX idx_notificacoes_tipo ON notificacoes(tipo);

CREATE TABLE notificacao_clientes (
    notificacao_id UUID NOT NULL REFERENCES notificacoes(id) ON DELETE CASCADE,
    cliente_id UUID NOT NULL,
    PRIMARY KEY (notificacao_id, cliente_id)
);

