package br.com.tourapp.tourapp.entity;

import br.com.tourapp.tourapp.enums.MetodoPagamento;
import br.com.tourapp.tourapp.enums.StatusPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
public class Pagamento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscricao_id", nullable = false)
    private Inscricao inscricao;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", nullable = false)
    private MetodoPagamento metodoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "mercadopago_payment_id")
    private String mercadoPagoPaymentId;

    @Column(name = "mercadopago_preference_id")
    private String mercadoPagoPreferenceId;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "qr_code_base64", columnDefinition = "TEXT")
    private String qrCodeBase64;

    @Column(name = "data_processamento")
    private LocalDateTime dataProcessamento;

    @Column(name = "data_vencimento")
    private LocalDateTime dataVencimento;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    // Construtores
    public Pagamento() {}

    public Pagamento(Inscricao inscricao, BigDecimal valor, MetodoPagamento metodoPagamento) {
        this.inscricao = inscricao;
        this.valor = valor;
        this.metodoPagamento = metodoPagamento;
    }

    // Getters e Setters
    public Inscricao getInscricao() { return inscricao; }
    public void setInscricao(Inscricao inscricao) { this.inscricao = inscricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public StatusPagamento getStatus() { return status; }
    public void setStatus(StatusPagamento status) { this.status = status; }

    public String getMercadoPagoPaymentId() { return mercadoPagoPaymentId; }
    public void setMercadoPagoPaymentId(String mercadoPagoPaymentId) { this.mercadoPagoPaymentId = mercadoPagoPaymentId; }

    public String getMercadoPagoPreferenceId() { return mercadoPagoPreferenceId; }
    public void setMercadoPagoPreferenceId(String mercadoPagoPreferenceId) { this.mercadoPagoPreferenceId = mercadoPagoPreferenceId; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }

    public LocalDateTime getDataProcessamento() { return dataProcessamento; }
    public void setDataProcessamento(LocalDateTime dataProcessamento) { this.dataProcessamento = dataProcessamento; }

    public LocalDateTime getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDateTime dataVencimento) { this.dataVencimento = dataVencimento; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}

