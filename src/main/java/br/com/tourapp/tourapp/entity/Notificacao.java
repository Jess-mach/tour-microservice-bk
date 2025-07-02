package br.com.tourapp.tourapp.entity;

import br.com.tourapp.tourapp.enums.TipoNotificacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notificacoes")
public class Notificacao extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Organizador organizador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "excursao_id")
    private Excursao excursao;

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 5, max = 100, message = "Título deve ter entre 5 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String titulo;

    @NotBlank(message = "Mensagem é obrigatória")
    @Size(min = 10, max = 500, message = "Mensagem deve ter entre 10 e 500 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacao tipo = TipoNotificacao.INFO;

    @Column(name = "enviada_em")
    private LocalDateTime enviadaEm;

    @ElementCollection
    @CollectionTable(name = "notificacao_clientes", joinColumns = @JoinColumn(name = "notificacao_id"))
    @Column(name = "cliente_id")
    private List<UUID> clientesAlvo;

    @Column(name = "enviar_para_todos", nullable = false)
    private Boolean enviarParaTodos = false;

    @Column(name = "enviada", nullable = false)
    private Boolean enviada = false;

    // Construtores
    public Notificacao() {}

    public Notificacao(Organizador organizador, String titulo, String mensagem, TipoNotificacao tipo) {
        this.organizador = organizador;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.tipo = tipo;
    }

    // Métodos auxiliares
    public boolean isEnviada() {
        return enviada != null && enviada;
    }

    public boolean podeSerEnviada() {
        return !isEnviada() && (enviarParaTodos || (clientesAlvo != null && !clientesAlvo.isEmpty()));
    }

    // Getters e Setters
    public Organizador getOrganizador() { return organizador; }
    public void setOrganizador(Organizador organizador) { this.organizador = organizador; }

    public Excursao getExcursao() { return excursao; }
    public void setExcursao(Excursao excursao) { this.excursao = excursao; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }

    public LocalDateTime getEnviadaEm() { return enviadaEm; }
    public void setEnviadaEm(LocalDateTime enviadaEm) { this.enviadaEm = enviadaEm; }

    public List<UUID> getClientesAlvo() { return clientesAlvo; }
    public void setClientesAlvo(List<UUID> clientesAlvo) { this.clientesAlvo = clientesAlvo; }

    public Boolean getEnviarParaTodos() { return enviarParaTodos; }
    public void setEnviarParaTodos(Boolean enviarParaTodos) { this.enviarParaTodos = enviarParaTodos; }

    public Boolean getEnviada() { return enviada; }
    public void setEnviada(Boolean enviada) { this.enviada = enviada; }
}