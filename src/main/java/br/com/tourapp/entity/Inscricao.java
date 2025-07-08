package br.com.tourapp.entity;

import br.com.tourapp.enums.StatusPagamento;
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
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

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

    //TODO companiaId //TODO resolver com a Claude

    // Construtores
    public Inscricao() {}

    public Inscricao(Excursao excursao, UserEntity user, BigDecimal valorPago) {
        this.excursao = excursao;
        this.user = user;
        this.valorPago = valorPago;
    }

    // Métodos auxiliares para compatibilidade
    public UserEntity getCliente() {
        return user;
    }

    public void setCliente(UserEntity user) {
        this.user = user;
    }

    // Getters e Setters
    public Excursao getExcursao() { return excursao; }
    public void setExcursao(Excursao excursao) { this.excursao = excursao; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(StatusPagamento statusPagamento) { this.statusPagamento = statusPagamento; }

    public String getObservacoesCliente() { return observacoesCliente; }
    public void setObservacoesCliente(String observacoesCliente) { this.observacoesCliente = observacoesCliente; }

    public List<Pagamento> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<Pagamento> pagamentos) { this.pagamentos = pagamentos; }
}