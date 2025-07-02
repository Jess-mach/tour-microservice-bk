package br.com.tourapp.tourapp.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExcursaoRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 5, max = 200, message = "Título deve ter entre 5 e 200 caracteres")
    private String titulo;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 2000, message = "Descrição deve ter entre 10 e 2000 caracteres")
    private String descricao;

    @NotNull(message = "Data de saída é obrigatória")
    @Future(message = "Data de saída deve ser no futuro")
    private LocalDateTime dataSaida;

    private LocalDateTime dataRetorno;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal preco;

    @NotNull(message = "Total de vagas é obrigatório")
    @Min(value = 1, message = "Deve ter pelo menos 1 vaga")
    private Integer vagasTotal;

    @Size(max = 300, message = "Local de saída deve ter no máximo 300 caracteres")
    private String localSaida;

    @Size(max = 300, message = "Local de destino deve ter no máximo 300 caracteres")
    private String localDestino;

    @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres")
    private String observacoes;

    private Boolean aceitaPix = true;
    private Boolean aceitaCartao = true;

    private List<MultipartFile> imagens;

    // Construtores
    public ExcursaoRequest() {}

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

    public String getLocalSaida() { return localSaida; }
    public void setLocalSaida(String localSaida) { this.localSaida = localSaida; }

    public String getLocalDestino() { return localDestino; }
    public void setLocalDestino(String localDestino) { this.localDestino = localDestino; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Boolean getAceitaPix() { return aceitaPix; }
    public void setAceitaPix(Boolean aceitaPix) { this.aceitaPix = aceitaPix; }

    public Boolean getAceitaCartao() { return aceitaCartao; }
    public void setAceitaCartao(Boolean aceitaCartao) { this.aceitaCartao = aceitaCartao; }

    public List<MultipartFile> getImagens() { return imagens; }
    public void setImagens(List<MultipartFile> imagens) { this.imagens = imagens; }
}