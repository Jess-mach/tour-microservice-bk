package br.com.tourapp.tourapp.entity;

import br.com.tourapp.tourapp.enums.StatusPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "inscricoes")
public class Inscricao extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "excursao_id", nullable = false)
    private Excursao excursao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull(message = "Valor pago é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(name = "valor_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", nullable = false)
    private StatusPagamento statusPagamento = StatusPagamento.PENDENTE;

    @Column(name = "observacoes_cliente", columnDefinition = "TEXT")
    private String observacoesCliente;

    @OneToMany(mappedBy = "inscricao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pagamento> pagamentos;

    // Construtores
    public Inscricao() {}

    public Inscricao(Excursao excursao, Cliente cliente, BigDecimal valorPago) {
        this.excursao = excursao;
        this.cliente = cliente;
        this.valorPago = valorPago;
    }

    // Getters e Setters
    public Excursao getExcursao() { return excursao; }
    public void setExcursao(Excursao excursao) { this.excursao = excursao; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(StatusPagamento statusPagamento) { this.statusPagamento = statusPagamento; }

    public String getObservacoesCliente() { return observacoesCliente; }
    public void setObservacoesCliente(String observacoesCliente) { this.observacoesCliente = observacoesCliente; }

    public List<Pagamento> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<Pagamento> pagamentos) { this.pagamentos = pagamentos; }
}
