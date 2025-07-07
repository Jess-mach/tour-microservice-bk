package br.com.tourapp.controller;

import br.com.tourapp.dto.response.DashboardResponse;
import br.com.tourapp.dto.response.InscricaoResponse;
import br.com.tourapp.dto.response.UserResponse;
import br.com.tourapp.service.OrganizadorService;
import br.com.tourapp.service.InscricaoService;
import br.com.tourapp.service.CompaniaSecurityService;
import br.com.tourapp.dto.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/organizador")
@PreAuthorize("hasRole('ORGANIZADOR')")
@Tag(name = "Organizador", description = "Endpoints para organizadores de excursões")
public class OrganizadorController {

    private final OrganizadorService organizadorService;
    private final InscricaoService inscricaoService;
    private final CompaniaSecurityService securityService;

    public OrganizadorController(OrganizadorService organizadorService,
                                 InscricaoService inscricaoService,
                                 CompaniaSecurityService securityService) {
        this.organizadorService = organizadorService;
        this.inscricaoService = inscricaoService;
        this.securityService = securityService;
    }

    // ============================================
    // PERFIL DO ORGANIZADOR
    // ============================================

    @GetMapping("/perfil")
    @Operation(summary = "Obter perfil", description = "Obtém o perfil do organizador")
    public ResponseEntity<UserResponse> obterPerfil(@AuthenticationPrincipal SecurityUser user) {
        UserResponse response = organizadorService.obterPerfil(user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/perfil")
    @Operation(summary = "Atualizar perfil", description = "Atualiza dados do perfil do organizador")
    public ResponseEntity<UserResponse> atualizarPerfil(
            @RequestBody UserResponse request,
            @AuthenticationPrincipal SecurityUser user) {
        UserResponse response = organizadorService.atualizarPerfil(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    // ============================================
    // DASHBOARD E ESTATÍSTICAS
    // ============================================

    @GetMapping("/dashboard")
    @Operation(summary = "Obter dashboard", description = "Obtém métricas e KPIs do organizador para uma compania específica")
    public ResponseEntity<DashboardResponse> obterDashboard(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) UUID companiaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        // Se não especificar compania, usar a primeira compania do usuário
        if (companiaId == null) {
            companiaId = securityService.obterCompaniaPadrao(user.getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não possui nenhuma compania"));
        }

        // Validar acesso à compania
        securityService.validarPermissaoVerFinanceiro(user.getId(), companiaId);

        DashboardResponse response = organizadorService.obterDashboard(user.getId(), companiaId, dataInicio, dataFim);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/companias")
    @Operation(summary = "Dashboard geral", description = "Obtém dashboard consolidado de todas as companias do usuário")
    public ResponseEntity<DashboardResponse> obterDashboardGeral(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        DashboardResponse response = organizadorService.obterDashboardConsolidado(user.getId(), dataInicio, dataFim);
        return ResponseEntity.ok(response);
    }

    // ============================================
    // GESTÃO DE INSCRIÇÕES
    // ============================================

    @GetMapping("/inscricoes")
    @Operation(summary = "Listar inscrições", description = "Lista inscrições das excursões do organizador")
    public ResponseEntity<Page<InscricaoResponse>> listarTodasInscricoes(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) UUID companiaId,
            @RequestParam(required = false) UUID excursaoId,
            Pageable pageable) {

        // Validar acesso à compania se especificada
        if (companiaId != null) {
            securityService.validarAcessoCompania(user.getId(), companiaId);
        }

        Page<InscricaoResponse> response = inscricaoService.listarInscricoesPorOrganizador(
                user.getId(), companiaId, excursaoId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/companias/{companiaId}/excursoes/{excursaoId}/inscricoes")
    @Operation(summary = "Listar inscrições por excursão", description = "Lista inscrições de uma excursão específica")
    @PreAuthorize("@companiaSecurityService.hasAccessToCompania(authentication.principal.id, #companiaId)")
    public ResponseEntity<Page<InscricaoResponse>> listarInscricoesPorExcursao(
            @PathVariable UUID companiaId,
            @PathVariable UUID excursaoId,
            @AuthenticationPrincipal SecurityUser user,
            Pageable pageable) {

        Page<InscricaoResponse> response = inscricaoService.listarInscricoesPorExcursao(
                excursaoId, companiaId, user.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    // ============================================
    // ESTATÍSTICAS ESPECÍFICAS
    // ============================================

    @GetMapping("/estatisticas/receita")
    @Operation(summary = "Estatísticas de receita", description = "Obtém estatísticas detalhadas de receita")
    public ResponseEntity<ReceitaEstatisticasResponse> obterEstatisticasReceita(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) UUID companiaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        if (companiaId != null) {
            securityService.validarPermissaoVerFinanceiro(user.getId(), companiaId);
        }

        ReceitaEstatisticasResponse response = organizadorService.obterEstatisticasReceita(
                user.getId(), companiaId, dataInicio, dataFim);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estatisticas/excursoes")
    @Operation(summary = "Estatísticas de excursões", description = "Obtém estatísticas das excursões")
    public ResponseEntity<ExcursoesEstatisticasResponse> obterEstatisticasExcursoes(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) UUID companiaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        if (companiaId != null) {
            securityService.validarAcessoCompania(user.getId(), companiaId);
        }

        ExcursoesEstatisticasResponse response = organizadorService.obterEstatisticasExcursoes(
                user.getId(), companiaId, dataInicio, dataFim);
        return ResponseEntity.ok(response);
    }

    // ============================================
    // RELATÓRIOS
    // ============================================

    @GetMapping("/relatorios/vendas")
    @Operation(summary = "Relatório de vendas", description = "Gera relatório detalhado de vendas")
    public ResponseEntity<RelatorioVendasResponse> gerarRelatorioVendas(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) UUID companiaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "false") Boolean incluirDetalhado) {

        if (companiaId != null) {
            securityService.validarPermissaoVerFinanceiro(user.getId(), companiaId);
        }

        RelatorioVendasResponse response = organizadorService.gerarRelatorioVendas(
                user.getId(), companiaId, dataInicio, dataFim, incluirDetalhado);
        return ResponseEntity.ok(response);
    }

    // ============================================
    // DTOs INTERNOS PARA RESPOSTAS
    // ============================================

    public static class ReceitaEstatisticasResponse {
        // Implementar campos necessários para estatísticas de receita
    }

    public static class ExcursoesEstatisticasResponse {
        // Implementar campos necessários para estatísticas de excursões
    }

    public static class RelatorioVendasResponse {
        // Implementar campos necessários para relatório de vendas
    }
}