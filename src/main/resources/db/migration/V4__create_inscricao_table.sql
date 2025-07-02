CREATE TABLE inscricoes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    excursao_id UUID NOT NULL REFERENCES excursoes(id) ON DELETE CASCADE,
    cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    valor_pago DECIMAL(10,2) NOT NULL,
    status_pagamento VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    observacoes_cliente TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT chk_valor_pago_positivo CHECK (valor_pago > 0),
    CONSTRAINT uk_inscricao_cliente_excursao UNIQUE (cliente_id, excursao_id)
);

CREATE INDEX idx_inscricoes_excursao ON inscricoes(excursao_id);
CREATE INDEX idx_inscricoes_cliente ON inscricoes(cliente_id);
CREATE INDEX idx_inscricoes_status ON inscricoes(status_pagamento);

