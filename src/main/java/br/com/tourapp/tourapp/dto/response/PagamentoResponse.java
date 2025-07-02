package br.com.tourapp.tourapp.dto.response;

import br.com.tourapp.tourapp.enums.MetodoPagamento;
import br.com.tourapp.tourapp.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PagamentoResponse {

    private UUID id;
    private BigDecimal valor;
    private MetodoPagamento metodoPagamento;
    private StatusPagamento status;
    private String qrCode;
    private String qrCodeBase64;
    private String mercadoPagoPaymentId;
    private LocalDateTime dataVencimento;
    private LocalDateTime dataProcessamento;
    private String linkPagamento;
    private String observacoes;

    // Construtores
    public PagamentoResponse() {}

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public StatusPagamento getStatus() { return status; }
    public void setStatus(StatusPagamento status) { this.status = status; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }

    public String getMercadoPagoPaymentId() { return mercadoPagoPaymentId; }
    public void setMercadoPagoPaymentId(String mercadoPagoPaymentId) { this.mercadoPagoPaymentId = mercadoPagoPaymentId; }

    public LocalDateTime getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDateTime dataVencimento) { this.dataVencimento = dataVencimento; }

    public LocalDateTime getDataProcessamento() { return dataProcessamento; }
    public void setDataProcessamento(LocalDateTime dataProcessamento) { this.dataProcessamento = dataProcessamento; }

    public String getLinkPagamento() { return linkPagamento; }
    public void setLinkPagamento(String linkPagamento) { this.linkPagamento = linkPagamento; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}