package br.com.tourapp.entity;

import br.com.tourapp.enums.StatusExcursao;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "excursoes")
public class Excursao extends BaseEntity {

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 5, max = 200, message = "Título deve ter entre 5 e 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 2000, message = "Descrição deve ter entre 10 e 2000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Data de saída é obrigatória")
    @Future(message = "Data de saída deve ser no futuro")
    @Column(name = "data_saida", nullable = false)
    private LocalDateTime dataSaida;

    @Column(name = "data_retorno")
    private LocalDateTime dataRetorno;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @NotNull(message = "Total de vagas é obrigatório")
    @Min(value = 1, message = "Deve ter pelo menos 1 vaga")
    @Column(name = "vagas_total", nullable = false)
    private Integer vagasTotal;

    @Column(name = "vagas_ocupadas", nullable = false)
    private Integer vagasOcupadas = 0;

    @Column(name = "local_saida", length = 300)
    private String localSaida;

    @Column(name = "local_destino", length = 300)
    private String localDestino;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @ElementCollection
    @CollectionTable(name = "excursao_imagens", joinColumns = @JoinColumn(name = "excursao_id"))
    @Column(name = "url_imagem")
    private List<String> imagens;

    @Column(name = "aceita_pix", nullable = false)
    private Boolean aceitaPix = true;

    @Column(name = "aceita_cartao", nullable = false)
    private Boolean aceitaCartao = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusExcursao status = StatusExcursao.RASCUNHO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Organizador organizador;

    @OneToMany(mappedBy = "excursao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscricao> inscricoes;

    // Construtores
    public Excursao() {}

    public Excursao(String titulo, String descricao, LocalDateTime dataSaida, BigDecimal preco,
                    Integer vagasTotal, Organizador organizador) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataSaida = dataSaida;
        this.preco = preco;
        this.vagasTotal = vagasTotal;
        this.organizador = organizador;
    }

    // Métodos auxiliares
    public Integer getVagasDisponiveis() {
        return vagasTotal - vagasOcupadas;
    }

    public boolean temVagasDisponiveis() {
        return getVagasDisponiveis() > 0;
    }

    public boolean isAtiva() {
        return status == StatusExcursao.ATIVA;
    }

    // Getters e Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }

    public LocalDateTime getDataRetorno() { return dataRetorno; }
    public void setDataRetorno(LocalDateTime dataRetorno) { this.dataRetorno = dataRetorno; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Integer getVagasTotal() { return vagasTotal; }
    public void setVagasTotal(Integer vagasTotal) { this.vagasTotal = vagasTotal; }

    public Integer getVagasOcupadas() { return vagasOcupadas; }
    public void setVagasOcupadas(Integer vagasOcupadas) { this.vagasOcupadas = vagasOcupadas; }

    public String getLocalSaida() { return localSaida; }
    public void setLocalSaida(String localSaida) { this.localSaida = localSaida; }

    public String getLocalDestino() { return localDestino; }
    public void setLocalDestino(String localDestino) { this.localDestino = localDestino; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public List<String> getImagens() { return imagens; }
    public void setImagens(List<String> imagens) { this.imagens = imagens; }

    public Boolean getAceitaPix() { return aceitaPix; }
    public void setAceitaPix(Boolean aceitaPix) { this.aceitaPix = aceitaPix; }

    public Boolean getAceitaCartao() { return aceitaCartao; }
    public void setAceitaCartao(Boolean aceitaCartao) { this.aceitaCartao = aceitaCartao; }

    public StatusExcursao getStatus() { return status; }
    public void setStatus(StatusExcursao status) { this.status = status; }

    public Organizador getOrganizador() { return organizador; }
    public void setOrganizador(Organizador organizador) { this.organizador = organizador; }

    public List<Inscricao> getInscricoes() { return inscricoes; }
    public void setInscricoes(List<Inscricao> inscricoes) { this.inscricoes = inscricoes; }
}