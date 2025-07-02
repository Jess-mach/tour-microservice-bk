package br.com.tourapp.dto.request;

import jakarta.validation.constraints.Size;

public class InscricaoRequest {

    @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres")
    private String observacoesCliente;

    // Construtores
    public InscricaoRequest() {}

    public InscricaoRequest(String observacoesCliente) {
        this.observacoesCliente = observacoesCliente;
    }

    // Getters e Setters
    public String getObservacoesCliente() { return observacoesCliente; }
    public void setObservacoesCliente(String observacoesCliente) { this.observacoesCliente = observacoesCliente; }
}