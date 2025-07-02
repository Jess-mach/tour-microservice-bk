package br.com.tourapp.tourapp.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class PagamentoPixRequest {

    @NotNull(message = "ID da inscrição é obrigatório")
    private UUID inscricaoId;

    // Construtores
    public PagamentoPixRequest() {}

    public PagamentoPixRequest(UUID inscricaoId) {
        this.inscricaoId = inscricaoId;
    }

    // Getters e Setters
    public UUID getInscricaoId() { return inscricaoId; }
    public void setInscricaoId(UUID inscricaoId) { this.inscricaoId = inscricaoId; }
}