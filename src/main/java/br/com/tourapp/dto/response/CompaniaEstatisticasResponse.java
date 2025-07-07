
// ============================================
// CompaniaEstatisticasResponse.java
// ============================================
package br.com.tourapp.dto.response;

import br.com.tourapp.dto.enums.RoleCompania;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class CompaniaEstatisticasResponse {

    // Estatísticas gerais
    private Long totalUsuarios;
    private Long totalUsuariosAtivos;
    private Long totalAdmins;
    private Long totalOrganizadores;
    private Long totalColaboradores;

    // Estatísticas de excursões
    private Long totalExcursoes;
    private Long totalExcursoesAtivas;
    private Long totalExcursoesRealizadas;
    private Long totalExcursoesCanceladas;

    // Estatísticas financeiras
    private BigDecimal receitaTotal;
    private BigDecimal receitaMesAtual;
    private BigDecimal receitaMesAnterior;
    private Double crescimentoReceita;

    // Estatísticas de inscrições
    private Long totalInscricoes;
    private Long totalInscricoesAprovadas;
    private Long totalInscricoesPendentes;

    // Estatísticas por período
    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    // Distribuição por estado/cidade
    private Map<String, Long> distribuicaoEstados;
    private Map<String, Long> distribuicaoCidades;

    // Top excursões
    private Map<String, Long> topExcursoesPorInscricoes;

    // Tendências
    private Map<String, Long> inscricoesPorMes;
    private Map<String, BigDecimal> receitaPorMes;
}
