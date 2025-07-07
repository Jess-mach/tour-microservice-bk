package br.com.tourapp.dto.request;

import br.com.tourapp.enums.StatusExcursao;

import java.util.UUID;

public class AlterarStatusLoteRequest {
    private java.util.List<UUID> excursaoIds;
    private StatusExcursao novoStatus;

    // Getters e Setters
    public java.util.List<UUID> getExcursaoIds() { return excursaoIds; }
    public void setExcursaoIds(java.util.List<UUID> excursaoIds) { this.excursaoIds = excursaoIds; }

    public StatusExcursao getNovoStatus() { return novoStatus; }
    public void setNovoStatus(StatusExcursao novoStatus) { this.novoStatus = novoStatus; }
}