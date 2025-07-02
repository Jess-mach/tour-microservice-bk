package br.com.tourapp.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class PagamentoCartaoRequest {

    @NotNull(message = "ID da inscrição é obrigatório")
    private UUID inscricaoId;

    @NotBlank(message = "Número do cartão é obrigatório")
    @Pattern(regexp = "\\d{13,19}", message = "Número do cartão deve conter apenas dígitos e ter entre 13 e 19 caracteres")
    private String numeroCartao;

    @NotBlank(message = "Nome no cartão é obrigatório")
    @Size(min = 2, max = 100, message = "Nome no cartão deve ter entre 2 e 100 caracteres")
    private String nomeCartao;

    @NotBlank(message = "Mês de expiração é obrigatório")
    @Pattern(regexp = "(0[1-9]|1[0-2])", message = "Mês deve estar no formato MM")
    private String mesExpiracao;

    @NotBlank(message = "Ano de expiração é obrigatório")
    @Pattern(regexp = "\\d{4}", message = "Ano deve estar no formato YYYY")
    private String anoExpiracao;

    @NotBlank(message = "CVV é obrigatório")
    @Pattern(regexp = "\\d{3,4}", message = "CVV deve conter 3 ou 4 dígitos")
    private String cvv;

    private Integer parcelas = 1;

    // Construtores
    public PagamentoCartaoRequest() {}

    // Getters e Setters
    public UUID getInscricaoId() { return inscricaoId; }
    public void setInscricaoId(UUID inscricaoId) { this.inscricaoId = inscricaoId; }

    public String getNumeroCartao() { return numeroCartao; }
    public void setNumeroCartao(String numeroCartao) { this.numeroCartao = numeroCartao; }

    public String getNomeCartao() { return nomeCartao; }
    public void setNomeCartao(String nomeCartao) { this.nomeCartao = nomeCartao; }

    public String getMesExpiracao() { return mesExpiracao; }
    public void setMesExpiracao(String mesExpiracao) { this.mesExpiracao = mesExpiracao; }

    public String getAnoExpiracao() { return anoExpiracao; }
    public void setAnoExpiracao(String anoExpiracao) { this.anoExpiracao = anoExpiracao; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }
}
