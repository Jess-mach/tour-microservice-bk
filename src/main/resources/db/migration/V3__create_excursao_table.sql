CREATE TABLE excursoes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organizador_id UUID NOT NULL REFERENCES organizadores(id) ON DELETE CASCADE,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT NOT NULL,
    data_saida TIMESTAMP NOT NULL,
    data_retorno TIMESTAMP,
    preco DECIMAL(10,2) NOT NULL,
    vagas_total INTEGER NOT NULL,
    vagas_ocupadas INTEGER NOT NULL DEFAULT 0,
    local_saida VARCHAR(300),
    local_destino VARCHAR(300),
    observacoes TEXT,
    aceita_pix BOOLEAN NOT NULL DEFAULT true,
    aceita_cartao BOOLEAN NOT NULL DEFAULT true,
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT chk_vagas_ocupadas CHECK (vagas_ocupadas >= 0),
    CONSTRAINT chk_vagas_total CHECK (vagas_total > 0),
    CONSTRAINT chk_vagas_ocupadas_menor_total CHECK (vagas_ocupadas <= vagas_total),
    CONSTRAINT chk_preco_positivo CHECK (preco > 0)
);

CREATE INDEX idx_excursoes_organizador ON excursoes(organizador_id);
CREATE INDEX idx_excursoes_status ON excursoes(status);
CREATE INDEX idx_excursoes_data_saida ON excursoes(data_saida);
CREATE INDEX idx_excursoes_ativas ON excursoes(status, data_saida) WHERE status = 'ATIVA';

CREATE TABLE excursao_imagens (
    excursao_id UUID NOT NULL REFERENCES excursoes(id) ON DELETE CASCADE,
    url_imagem VARCHAR(500) NOT NULL,
    PRIMARY KEY (excursao_id, url_imagem)
);

