package br.com.tourapp.dto.request;

import java.util.UUID;

public  class ExcluirLoteRequest {
    private java.util.List<UUID> excursaoIds;
    private String motivo;

    // Getters e Setters
    public java.util.List<UUID> getExcursaoIds() { return excursaoIds; }
    public void setExcursaoIds(java.util.List<UUID> excursaoIds) { this.excursaoIds = excursaoIds; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}