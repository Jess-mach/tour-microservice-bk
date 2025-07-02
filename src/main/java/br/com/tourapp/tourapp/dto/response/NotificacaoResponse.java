package br.com.tourapp.tourapp.dto.response;

import br.com.tourapp.tourapp.enums.TipoNotificacao;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificacaoResponse {

    private UUID id;
    private String titulo;
    private String mensagem;
    private TipoNotificacao tipo;
    private LocalDateTime enviadaEm;
    private Boolean enviarParaTodos;
    private Boolean enviada;
    private LocalDateTime createdAt;

    // Dados da excursão (opcional)
    private UUID excursaoId;
    private String tituloExcursao;

    // Estatísticas
    private Long totalDestinatarios;

    // Construtores
    public NotificacaoResponse() {}

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }

    public LocalDateTime getEnviadaEm() { return enviadaEm; }
    public void setEnviadaEm(LocalDateTime enviadaEm) { this.enviadaEm = enviadaEm; }

    public Boolean getEnviarParaTodos() { return enviarParaTodos; }
    public void setEnviarParaTodos(Boolean enviarParaTodos) { this.enviarParaTodos = enviarParaTodos; }

    public Boolean getEnviada() { return enviada; }
    public void setEnviada(Boolean enviada) { this.enviada = enviada; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UUID getExcursaoId() { return excursaoId; }
    public void setExcursaoId(UUID excursaoId) { this.excursaoId = excursaoId; }

    public String getTituloExcursao() { return tituloExcursao; }
    public void setTituloExcursao(String tituloExcursao) { this.tituloExcursao = tituloExcursao; }

    public Long getTotalDestinatarios() { return totalDestinatarios; }
    public void setTotalDestinatarios(Long totalDestinatarios) { this.totalDestinatarios = totalDestinatarios; }
}