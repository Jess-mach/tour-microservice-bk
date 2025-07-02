CREATE TABLE pagamentos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    inscricao_id UUID NOT NULL REFERENCES inscricoes(id) ON DELETE CASCADE,
    valor DECIMAL(10,2) NOT NULL,
    metodo_pagamento VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    mercadopago_payment_id VARCHAR(100),
    mercadopago_preference_id VARCHAR(100),
    qr_code VARCHAR(500),
    qr_code_base64 TEXT,
    data_processamento TIMESTAMP,
    data_vencimento TIMESTAMP,
    observacoes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT chk_valor_positivo CHECK (valor > 0)
);

CREATE INDEX idx_pagamentos_inscricao ON pagamentos(inscricao_id);
CREATE INDEX idx_pagamentos_status ON pagamentos(status);
CREATE INDEX idx_pagamentos_mercadopago ON pagamentos(mercadopago_payment_id);
CREATE INDEX idx_pagamentos_metodo ON pagamentos(metodo_pagamento);

