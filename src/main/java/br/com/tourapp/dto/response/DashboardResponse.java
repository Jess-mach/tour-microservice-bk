package br.com.tourapp.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DashboardResponse {

    private Long totalExcursoes;
    private Long excursoesMes;
    private Long excursoesAtivas;
    private BigDecimal receitaTotal;
    private BigDecimal receitaMes;
    private Long totalInscricoes;
    private Long inscricoesPendentes;
    private Long inscricoesAprovadas;
    private Double taxaOcupacaoMedia;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    // Métricas detalhadas
    private Long clientesUnicos;
    private BigDecimal ticketMedio;
    private Long notificacoesEnviadas;

    // Construtores
    public DashboardResponse() {}

    // Getters e Setters
    public Long getTotalExcursoes() { return totalExcursoes; }
    public void setTotalExcursoes(Long totalExcursoes) { this.totalExcursoes = totalExcursoes; }

    public Long getExcursoesMes() { return excursoesMes; }
    public void setExcursoesMes(Long excursoesMes) { this.excursoesMes = excursoesMes; }

    public Long getExcursoesAtivas() { return excursoesAtivas; }
    public void setExcursoesAtivas(Long excursoesAtivas) { this.excursoesAtivas = excursoesAtivas; }

    public BigDecimal getReceitaTotal() { return receitaTotal; }
    public void setReceitaTotal(BigDecimal receitaTotal) { this.receitaTotal = receitaTotal; }

    public BigDecimal getReceitaMes() { return receitaMes; }
    public void setReceitaMes(BigDecimal receitaMes) { this.receitaMes = receitaMes; }

    public Long getTotalInscricoes() { return totalInscricoes; }
    public void setTotalInscricoes(Long totalInscricoes) { this.totalInscricoes = totalInscricoes; }

    public Long getInscricoesPendentes() { return inscricoesPendentes; }
    public void setInscricoesPendentes(Long inscricoesPendentes) { this.inscricoesPendentes = inscricoesPendentes; }

    public Long getInscricoesAprovadas() { return inscricoesAprovadas; }
    public void setInscricoesAprovadas(Long inscricoesAprovadas) { this.inscricoesAprovadas = inscricoesAprovadas; }

    public Double getTaxaOcupacaoMedia() { return taxaOcupacaoMedia; }
    public void setTaxaOcupacaoMedia(Double taxaOcupacaoMedia) { this.taxaOcupacaoMedia = taxaOcupacaoMedia; }

    public LocalDate getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(LocalDate periodoInicio) { this.periodoInicio = periodoInicio; }

    public LocalDate getPeriodoFim() { return periodoFim; }
    public void setPeriodoFim(LocalDate periodoFim) { this.periodoFim = periodoFim; }

    public Long getClientesUnicos() { return clientesUnicos; }
    public void setClientesUnicos(Long clientesUnicos) { this.clientesUnicos = clientesUnicos; }

    public BigDecimal getTicketMedio() { return ticketMedio; }
    public void setTicketMedio(BigDecimal ticketMedio) { this.ticketMedio = ticketMedio; }

    public Long getNotificacoesEnviadas() { return notificacoesEnviadas; }
    public void setNotificacoesEnviadas(Long notificacoesEnviadas) { this.notificacoesEnviadas = notificacoesEnviadas; }
}