package br.com.tourapp.tourapp.dto.response;

import br.com.tourapp.tourapp.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class InscricaoResponse {

    private UUID id;
    private BigDecimal valorPago;
    private StatusPagamento statusPagamento;
    private String observacoesCliente;
    private LocalDateTime createdAt;

    // Dados da excursão
    private UUID excursaoId;
    private String tituloExcursao;
    private LocalDateTime dataSaidaExcursao;
    private LocalDateTime dataRetornoExcursao;
    private String localSaidaExcursao;
    private String localDestinoExcursao;

    // Dados do cliente (para organizador)
    private UUID clienteId;
    private String nomeCliente;
    private String emailCliente;
    private String telefoneCliente;

    // Construtores
    public InscricaoResponse() {}

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(StatusPagamento statusPagamento) { this.statusPagamento = statusPagamento; }

    public String getObservacoesCliente() { return observacoesCliente; }
    public void setObservacoesCliente(String observacoesCliente) { this.observacoesCliente = observacoesCliente; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UUID getExcursaoId() { return excursaoId; }
    public void setExcursaoId(UUID excursaoId) { this.excursaoId = excursaoId; }

    public String getTituloExcursao() { return tituloExcursao; }
    public void setTituloExcursao(String tituloExcursao) { this.tituloExcursao = tituloExcursao; }

    public LocalDateTime getDataSaidaExcursao() { return dataSaidaExcursao; }
    public void setDataSaidaExcursao(LocalDateTime dataSaidaExcursao) { this.dataSaidaExcursao = dataSaidaExcursao; }

    public LocalDateTime getDataRetornoExcursao() { return dataRetornoExcursao; }
    public void setDataRetornoExcursao(LocalDateTime dataRetornoExcursao) { this.dataRetornoExcursao = dataRetornoExcursao; }

    public String getLocalSaidaExcursao() { return localSaidaExcursao; }
    public void setLocalSaidaExcursao(String localSaidaExcursao) { this.localSaidaExcursao = localSaidaExcursao; }

    public String getLocalDestinoExcursao() { return localDestinoExcursao; }
    public void setLocalDestinoExcursao(String localDestinoExcursao) { this.localDestinoExcursao = localDestinoExcursao; }

    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }

    public String getTelefoneCliente() { return telefoneCliente; }
    public void setTelefoneCliente(String telefoneCliente) { this.telefoneCliente = telefoneCliente; }
}
