package br.com.tourapp.controller;

import br.com.tourapp.dto.request.ExcursaoRequest;
import br.com.tourapp.dto.response.ExcursaoResponse;
import br.com.tourapp.enums.StatusExcursao;
import br.com.tourapp.dto.SecurityUser;
import br.com.tourapp.service.ExcursaoService;
import br.com.tourapp.service.CompaniaSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/organizador/excursoes")
@PreAuthorize("hasRole('ORGANIZADOR')")
@Tag(name = "Excursões", description = "Gestão de excursões pelo organizador")
public class ExcursaoController {

    private final ExcursaoService excursaoService;
    private final CompaniaSecurityService securityService;

    public ExcursaoController(ExcursaoService excursaoService, CompaniaSecurityService securityService) {
        this.excursaoService = excursaoService;
        this.securityService = securityService;
    }

    // ============================================
    // CRUD DE EXCURSÕES
    // ============================================

    @PostMapping
    @Operation(summary = "Criar excursão", description = "Cria uma nova excursão para a compania")
    public ResponseEntity<ExcursaoResponse> criarExcursao(
            @Valid @ModelAttribute ExcursaoRequest request,
            @RequestParam UUID companiaId,
            @AuthenticationPrincipal SecurityUser user) {

        // Validar permissão para criar excursões na compania
        securityService.validarPermissaoCriarExcursoes(user.getId(), companiaId);

        ExcursaoResponse response = excursaoService.criarExcursao(request, companiaId, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar excursões", description = "Lista excursões do organizador filtradas por compania")
    public ResponseEntity<Page<ExcursaoResponse>> listarExcursoes(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) UUID companiaId,
            @RequestParam(required = false) StatusExcursao status,
            Pageable pageable) {

        // Se companiaId não for especificado, listar de todas as companias do usuário
        if (companiaId != null) {
            securityService.validarAcessoCompania(user.getId(), companiaId);
        }

        Page<ExcursaoResponse> response = excursaoService.listarExcursoesPorOrganizador(
                user.getId(), companiaId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter excursão", description = "Obtém detalhes de uma excursão específica")
    @PreAuthorize("@companiaSecurityService.canEditExcursion(authentication.principal.id, #id)")
    public ResponseEntity<ExcursaoResponse> obterExcursao(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {

        ExcursaoResponse response = excursaoService.obterExcursaoPorOrganizador(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar excursão", description = "Atualiza dados de uma excursão")
    @PreAuthorize("@companiaSecurityService.canEditExcursion(authentication.principal.id, #id)")
    public ResponseEntity<ExcursaoResponse> atualizarExcursao(
            @PathVariable UUID id,
            @Valid @ModelAttribute ExcursaoRequest request,
            @AuthenticationPrincipal SecurityUser user) {

        ExcursaoResponse response = excursaoService.atualizarExcursao(id, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status", description = "Altera o status de uma excursão")
    @PreAuthorize("@companiaSecurityService.canEditExcursion(authentication.principal.id, #id)")
    public ResponseEntity<ExcursaoResponse> alterarStatus(
            @PathVariable UUID id,
            @RequestParam StatusExcursao status,
            @AuthenticationPrincipal SecurityUser user) {

        ExcursaoResponse response = excursaoService.alterarStatusExcursao(id, status, user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir excursão", description = "Exclui uma excursão (apenas se não houver inscrições)")
    @PreAuthorize("@companiaSecurityService.canEditExcursion(authentication.principal.id, #id)")
    public ResponseEntity<Void> excluirExcursao(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {

        excursaoService.excluirExcursao(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // ENDPOINTS ESPECÍFICOS POR COMPANIA
    // ============================================

    @GetMapping("/compania/{companiaId}")
    @Operation(summary = "Listar excursões da compania", description = "Lista todas as excursões de uma compania específica")
    @PreAuthorize("@companiaSecurityService.hasAccessToCompania(authentication.principal.id, #companiaId)")
    public ResponseEntity<Page<ExcursaoResponse>> listarExcursoesPorCompania(
            @PathVariable UUID companiaId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) StatusExcursao status,
            Pageable pageable) {

        Page<ExcursaoResponse> response = excursaoService.listarExcursoesPorCompania(
                companiaId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/compania/{companiaId}")
    @Operation(summary = "Criar excursão na compania", description = "Cria uma nova excursão em uma compania específica")
    @PreAuthorize("@companiaSecurityService.canCreateExcursions(authentication.principal.id, #companiaId)")
    public ResponseEntity<ExcursaoResponse> criarExcursaoNaCompania(
            @PathVariable UUID companiaId,
            @Valid @ModelAttribute ExcursaoRequest request,
            @AuthenticationPrincipal SecurityUser user) {

        ExcursaoResponse response = excursaoService.criarExcursao(request, companiaId, user.getId());
        return ResponseEntity.ok(response);
    }

    // ============================================
    // OPERAÇÕES EM LOTE
    // ============================================

    @PatchMapping("/lote/status")
    @Operation(summary = "Alterar status em lote", description = "Altera o status de múltiplas excursões")
    public ResponseEntity<LoteOperacaoResponse> alterarStatusLote(
            @RequestBody AlterarStatusLoteRequest request,
            @AuthenticationPrincipal SecurityUser user) {

        // Validar permissões para cada excursão
        for (UUID excursaoId : request.getExcursaoIds()) {
            securityService.validarPermissaoEditarExcursao(user.getId(), excursaoId);
        }

        LoteOperacaoResponse response = excursaoService.alterarStatusLote(
                request.getExcursaoIds(), request.getNovoStatus(), user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/lote")
    @Operation(summary = "Excluir em lote", description = "Exclui múltiplas excursões")
    public ResponseEntity<LoteOperacaoResponse> excluirExcursoesLote(
            @RequestBody ExcluirLoteRequest request,
            @AuthenticationPrincipal SecurityUser user) {

        // Validar permissões para cada excursão
        for (UUID excursaoId : request.getExcursaoIds()) {
            securityService.validarPermissaoEditarExcursao(user.getId(), excursaoId);
        }

        LoteOperacaoResponse response = excursaoService.excluirExcursoesLote(
                request.getExcursaoIds(), user.getId());
        return ResponseEntity.ok(response);
    }

    // ============================================
    // ESTATÍSTICAS E RELATÓRIOS
    // ============================================

    @GetMapping("/{id}/estatisticas")
    @Operation(summary = "Estatísticas da excursão", description = "Obtém estatísticas detalhadas de uma excursão")
    @PreAuthorize("@companiaSecurityService.canEditExcursion(authentication.principal.id, #id)")
    public ResponseEntity<ExcursaoEstatisticasResponse> obterEstatisticasExcursao(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {

        ExcursaoEstatisticasResponse response = excursaoService.obterEstatisticasExcursao(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estatisticas/resumo")
    @Operation(summary = "Resumo estatístico", description = "Obtém resumo estatístico das excursões do organizador")
    public ResponseEntity<ResumoEstatisticasResponse> obterResumoEstatisticas(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) UUID companiaId) {

        if (companiaId != null) {
            securityService.validarAcessoCompania(user.getId(), companiaId);
        }

        ResumoEstatisticasResponse response = excursaoService.obterResumoEstatisticas(
                user.getId(), companiaId);
        return ResponseEntity.ok(response);
    }

    // ============================================
    // DUPLICAÇÃO E TEMPLATES
    // ============================================

    @PostMapping("/{id}/duplicar")
    @Operation(summary = "Duplicar excursão", description = "Cria uma nova excursão baseada em uma existente")
    @PreAuthorize("@companiaSecurityService.canEditExcursion(authentication.principal.id, #id)")
    public ResponseEntity<ExcursaoResponse> duplicarExcursao(
            @PathVariable UUID id,
            @RequestBody DuplicarExcursaoRequest request,
            @AuthenticationPrincipal SecurityUser user) {

        ExcursaoResponse response = excursaoService.duplicarExcursao(id, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/salvar-template")
    @Operation(summary = "Salvar como template", description = "Salva a excursão como template para uso futuro")
    @PreAuthorize("@companiaSecurityService.canEditExcursion(authentication.principal.id, #id)")
    public ResponseEntity<TemplateResponse> salvarComoTemplate(
            @PathVariable UUID id,
            @RequestBody SalvarTemplateRequest request,
            @AuthenticationPrincipal SecurityUser user) {

        TemplateResponse response = excursaoService.salvarComoTemplate(id, request, user.getId());
        return ResponseEntity.ok(response);
    }

    // ============================================
    // DTOs INTERNOS PARA REQUESTS/RESPONSES
    // ============================================

    public static class AlterarStatusLoteRequest {
        private java.util.List<UUID> excursaoIds;
        private StatusExcursao novoStatus;

        // Getters e Setters
        public java.util.List<UUID> getExcursaoIds() { return excursaoIds; }
        public void setExcursaoIds(java.util.List<UUID> excursaoIds) { this.excursaoIds = excursaoIds; }

        public StatusExcursao getNovoStatus() { return novoStatus; }
        public void setNovoStatus(StatusExcursao novoStatus) { this.novoStatus = novoStatus; }
    }

    public static class ExcluirLoteRequest {
        private java.util.List<UUID> excursaoIds;
        private String motivo;

        // Getters e Setters
        public java.util.List<UUID> getExcursaoIds() { return excursaoIds; }
        public void setExcursaoIds(java.util.List<UUID> excursaoIds) { this.excursaoIds = excursaoIds; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
    }

    public static class DuplicarExcursaoRequest {
        private String novoTitulo;
        private java.time.LocalDateTime novaDataSaida;
        private java.time.LocalDateTime novaDataRetorno;
        private java.math.BigDecimal novoPreco;

        // Getters e Setters
        public String getNovoTitulo() { return novoTitulo; }
        public void setNovoTitulo(String novoTitulo) { this.novoTitulo = novoTitulo; }

        public java.time.LocalDateTime getNovaDataSaida() { return novaDataSaida; }
        public void setNovaDataSaida(java.time.LocalDateTime novaDataSaida) { this.novaDataSaida = novaDataSaida; }

        public java.time.LocalDateTime getNovaDataRetorno() { return novaDataRetorno; }
        public void setNovaDataRetorno(java.time.LocalDateTime novaDataRetorno) { this.novaDataRetorno = novaDataRetorno; }

        public java.math.BigDecimal getNovoPreco() { return novoPreco; }
        public void setNovoPreco(java.math.BigDecimal novoPreco) { this.novoPreco = novoPreco; }
    }

    public static class SalvarTemplateRequest {
        private String nomeTemplate;
        private String descricaoTemplate;
        private Boolean publico = false;

        // Getters e Setters
        public String getNomeTemplate() { return nomeTemplate; }
        public void setNomeTemplate(String nomeTemplate) { this.nomeTemplate = nomeTemplate; }

        public String getDescricaoTemplate() { return descricaoTemplate; }
        public void setDescricaoTemplate(String descricaoTemplate) { this.descricaoTemplate = descricaoTemplate; }

        public Boolean getPublico() { return publico; }
        public void setPublico(Boolean publico) { this.publico = publico; }
    }

    public static class LoteOperacaoResponse {
        private Integer totalProcessados;
        private Integer sucessos;
        private Integer erros;
        private java.util.List<String> mensagensErro;

        // Getters e Setters
        public Integer getTotalProcessados() { return totalProcessados; }
        public void setTotalProcessados(Integer totalProcessados) { this.totalProcessados = totalProcessados; }

        public Integer getSucessos() { return sucessos; }
        public void setSucessos(Integer sucessos) { this.sucessos = sucessos; }

        public Integer getErros() { return erros; }
        public void setErros(Integer erros) { this.erros = erros; }

        public java.util.List<String> getMensagensErro() { return mensagensErro; }
        public void setMensagensErro(java.util.List<String> mensagensErro) { this.mensagensErro = mensagensErro; }
    }

    public static class ExcursaoEstatisticasResponse {
        private Long totalInscricoes;
        private Long inscricoesAprovadas;
        private Long inscricoesPendentes;
        private java.math.BigDecimal receitaTotal;
        private java.math.BigDecimal receitaConfirmada;
        private Double taxaOcupacao;
        private Integer vagasDisponiveis;

        // Getters e Setters
        public Long getTotalInscricoes() { return totalInscricoes; }
        public void setTotalInscricoes(Long totalInscricoes) { this.totalInscricoes = totalInscricoes; }

        public Long getInscricoesAprovadas() { return inscricoesAprovadas; }
        public void setInscricoesAprovadas(Long inscricoesAprovadas) { this.inscricoesAprovadas = inscricoesAprovadas; }

        public Long getInscricoesPendentes() { return inscricoesPendentes; }
        public void setInscricoesPendentes(Long inscricoesPendentes) { this.inscricoesPendentes = inscricoesPendentes; }

        public java.math.BigDecimal getReceitaTotal() { return receitaTotal; }
        public void setReceitaTotal(java.math.BigDecimal receitaTotal) { this.receitaTotal = receitaTotal; }

        public java.math.BigDecimal getReceitaConfirmada() { return receitaConfirmada; }
        public void setReceitaConfirmada(java.math.BigDecimal receitaConfirmada) { this.receitaConfirmada = receitaConfirmada; }

        public Double getTaxaOcupacao() { return taxaOcupacao; }
        public void setTaxaOcupacao(Double taxaOcupacao) { this.taxaOcupacao = taxaOcupacao; }

        public Integer getVagasDisponiveis() { return vagasDisponiveis; }
        public void setVagasDisponiveis(Integer vagasDisponiveis) { this.vagasDisponiveis = vagasDisponiveis; }
    }

    public static class ResumoEstatisticasResponse {
        private Long totalExcursoes;
        private Long excursoesAtivas;
        private Long excursoesRealizadas;
        private java.math.BigDecimal receitaTotalMes;
        private java.math.BigDecimal receitaTotalAno;

        // Getters e Setters
        public Long getTotalExcursoes() { return totalExcursoes; }
        public void setTotalExcursoes(Long totalExcursoes) { this.totalExcursoes = totalExcursoes; }

        public Long getExcursoesAtivas() { return excursoesAtivas; }
        public void setExcursoesAtivas(Long excursoesAtivas) { this.excursoesAtivas = excursoesAtivas; }

        public Long getExcursoesRealizadas() { return excursoesRealizadas; }
        public void setExcursoesRealizadas(Long excursoesRealizadas) { this.excursoesRealizadas = excursoesRealizadas; }

        public java.math.BigDecimal getReceitaTotalMes() { return receitaTotalMes; }
        public void setReceitaTotalMes(java.math.BigDecimal receitaTotalMes) { this.receitaTotalMes = receitaTotalMes; }

        public java.math.BigDecimal getReceitaTotalAno() { return receitaTotalAno; }
        public void setReceitaTotalAno(java.math.BigDecimal receitaTotalAno) { this.receitaTotalAno = receitaTotalAno; }
    }

    public static class TemplateResponse {
        private UUID id;
        private String nome;
        private String descricao;
        private Boolean publico;
        private java.time.LocalDateTime createdAt;

        // Getters e Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }

        public Boolean getPublico() { return publico; }
        public void setPublico(Boolean publico) { this.publico = publico; }

        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}