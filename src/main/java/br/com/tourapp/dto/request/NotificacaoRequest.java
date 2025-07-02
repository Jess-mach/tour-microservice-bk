package br.com.tourapp.dto.request;

import br.com.tourapp.enums.TipoNotificacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class NotificacaoRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 5, max = 100, message = "Título deve ter entre 5 e 100 caracteres")
    private String titulo;

    @NotBlank(message = "Mensagem é obrigatória")
    @Size(min = 10, max = 500, message = "Mensagem deve ter entre 10 e 500 caracteres")
    private String mensagem;

    private TipoNotificacao tipo = TipoNotificacao.INFO;
    private UUID excursaoId;
    private List<UUID> clientesAlvo;
    private Boolean enviarParaTodos = false;

    // Construtores
    public NotificacaoRequest() {}

    // Getters e Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }

    public UUID getExcursaoId() { return excursaoId; }
    public void setExcursaoId(UUID excursaoId) { this.excursaoId = excursaoId; }

    public List<UUID> getClientesAlvo() { return clientesAlvo; }
    public void setClientesAlvo(List<UUID> clientesAlvo) { this.clientesAlvo = clientesAlvo; }

    public Boolean getEnviarParaTodos() { return enviarParaTodos; }
    public void setEnviarParaTodos(Boolean enviarParaTodos) { this.enviarParaTodos = enviarParaTodos; }
}
