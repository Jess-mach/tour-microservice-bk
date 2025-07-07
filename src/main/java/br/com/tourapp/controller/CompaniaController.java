package br.com.tourapp.controller;

import br.com.tourapp.dto.SecurityUser;
import br.com.tourapp.dto.request.CompaniaRequest;
import br.com.tourapp.dto.request.UserCompaniaRequest;
import br.com.tourapp.dto.request.AlterarRoleRequest;
import br.com.tourapp.dto.response.CompaniaResponse;
import br.com.tourapp.dto.response.UserCompaniaResponse;
import br.com.tourapp.dto.response.CompaniaUsuarioResponse;
import br.com.tourapp.dto.response.CompaniaEstatisticasResponse;
import br.com.tourapp.service.CompaniaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organizador/companias")
@PreAuthorize("hasRole('ORGANIZADOR')")
@Tag(name = "Compania", description = "Gestão de empresas e organizadores")
public class CompaniaController {

    private final CompaniaService companiaService;

    public CompaniaController(CompaniaService companiaService) {
        this.companiaService = companiaService;
    }

    // ============================================
    // CRUD BÁSICO DE COMPANIAS
    // ============================================

    @PostMapping
    @Operation(summary = "Criar nova compania", description = "Cria uma nova empresa e adiciona o usuário como administrador")
    public ResponseEntity<CompaniaResponse> criarCompania(
            @Valid @RequestBody CompaniaRequest request,
            @AuthenticationPrincipal SecurityUser user) {

        CompaniaResponse response = companiaService.criarCompania(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar minhas companias", description = "Lista todas as companias que o usuário faz parte")
    public ResponseEntity<List<CompaniaResponse>> listarMinhasCompanias(
            @AuthenticationPrincipal SecurityUser user) {

        List<CompaniaResponse> response = companiaService.listarMinhasCompanias(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{companiaId}")
    @Operation(summary = "Obter compania", description = "Obtém detalhes de uma compania específica")
    @PreAuthorize("@companiaSecurityService.hasAccessToCompania(authentication.principal.id, #companiaId)")
    public ResponseEntity<CompaniaResponse> obterCompania(
            @PathVariable UUID companiaId,
            @AuthenticationPrincipal SecurityUser user) {

        CompaniaResponse response = companiaService.obterCompania(companiaId, user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{companiaId}")
    @Operation(summary = "Atualizar compania", description = "Atualiza dados da compania")
    @PreAuthorize("@companiaSecurityService.canEditCompania(authentication.principal.id, #companiaId)")
    public ResponseEntity<CompaniaResponse> atualizarCompania(
            @PathVariable UUID companiaId,
            @Valid @RequestBody CompaniaRequest request,
            @AuthenticationPrincipal SecurityUser user) {

        CompaniaResponse response = companiaService.atualizarCompania(companiaId, request, user.getId());
        return ResponseEntity.ok(response);
    }

    // ============================================
    // GESTÃO DE USUÁRIOS
    // ============================================

    @GetMapping("/{companiaId}/usuarios")
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários da compania")
    @PreAuthorize("@companiaSecurityService.hasAccessToCompania(authentication.principal.id, #companiaId)")
    public ResponseEntity<Page<CompaniaUsuarioResponse>> listarUsuarios(
            @PathVariable UUID companiaId,
            @AuthenticationPrincipal SecurityUser user,
            Pageable pageable) {

        Page<CompaniaUsuarioResponse> response = companiaService.listarUsuariosCompania(
                companiaId, user.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{companiaId}/usuarios")
    @Operation(summary = "Adicionar usuário", description = "Adiciona um novo usuário à compania")
    @PreAuthorize("@companiaSecurityService.canManageUsers(authentication.principal.id, #companiaId)")
    public ResponseEntity<UserCompaniaResponse> adicionarUsuario(
            @PathVariable UUID companiaId,
            @Valid @RequestBody UserCompaniaRequest request,
            @AuthenticationPrincipal SecurityUser user) {

        // Garantir que o companiaId do path seja usado
        request.setCompaniaId(companiaId);

        UserCompaniaResponse response = companiaService.adicionarUsuario(companiaId, request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{companiaId}/usuarios/role")
    @Operation(summary = "Alterar role do usuário", description = "Altera o papel de um usuário na compania")
    @PreAuthorize("@companiaSecurityService.canManageUsers(authentication.principal.id, #companiaId)")
    public ResponseEntity<UserCompaniaResponse> alterarRoleUsuario(
            @PathVariable UUID companiaId,
            @Valid @RequestBody AlterarRoleRequest request,
            @AuthenticationPrincipal SecurityUser user) {

        UserCompaniaResponse response = companiaService.alterarRoleUsuario(companiaId, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{companiaId}/usuarios/{userId}")
    @Operation(summary = "Remover usuário", description = "Remove um usuário da compania")
    @PreAuthorize("@companiaSecurityService.canManageUsers(authentication.principal.id, #companiaId)")
    public ResponseEntity<Void> removerUsuario(
            @PathVariable UUID companiaId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal SecurityUser user) {

        companiaService.removerUsuario(companiaId, userId, user.getId());
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // ESTATÍSTICAS E RELATÓRIOS
    // ============================================

    @GetMapping("/{companiaId}/estatisticas")
    @Operation(summary = "Obter estatísticas", description = "Obtém estatísticas detalhadas da compania")
    @PreAuthorize("@companiaSecurityService.canViewFinancial(authentication.principal.id, #companiaId)")
    public ResponseEntity<CompaniaEstatisticasResponse> obterEstatisticas(
            @PathVariable UUID companiaId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        CompaniaEstatisticasResponse response = companiaService.obterEstatisticas(
                companiaId, user.getId(), dataInicio, dataFim);
        return ResponseEntity.ok(response);
    }

    // ============================================
    // ENDPOINTS DE CONVENIÊNCIA
    // ============================================

    @GetMapping("/minha-compania-padrao")
    @Operation(summary = "Obter compania padrão", description = "Obtém a primeira compania ativa do usuário")
    public ResponseEntity<CompaniaResponse> obterCompaniaPadrao(
            @AuthenticationPrincipal SecurityUser user) {

        List<CompaniaResponse> companias = companiaService.listarMinhasCompanias(user.getId());

        if (companias.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Retorna a primeira compania ativa
        CompaniaResponse companiaPadrao = companias.stream()
                .filter(c -> "ATIVA".equals(c.getStatus().name()))
                .findFirst()
                .orElse(companias.get(0));

        return ResponseEntity.ok(companiaPadrao);
    }

    @GetMapping("/{companiaId}/permissoes")
    @Operation(summary = "Verificar permissões", description = "Verifica as permissões do usuário na compania")
    @PreAuthorize("@companiaSecurityService.hasAccessToCompania(authentication.principal.id, #companiaId)")
    public ResponseEntity<PermissoesResponse> verificarPermissoes(
            @PathVariable UUID companiaId,
            @AuthenticationPrincipal SecurityUser user) {

        CompaniaResponse compania = companiaService.obterCompania(companiaId, user.getId());

        PermissoesResponse permissoes = new PermissoesResponse();
        permissoes.setPodeCreiarExcursoes(compania.getPodeCreiarExcursoes());
        permissoes.setPodeGerenciarUsuarios(compania.getPodeGerenciarUsuarios());
        permissoes.setPodeVerFinanceiro(compania.getPodeVerFinanceiro());
        permissoes.setPodeEditarCompania(compania.getPodeEditarCompania());
        permissoes.setPodeEnviarNotificacoes(compania.getPodeEnviarNotificacoes());
        permissoes.setRoleCompania(compania.getRoleUsuario());

        return ResponseEntity.ok(permissoes);
    }

    // ============================================
    // DTO INTERNO PARA PERMISSÕES
    // ============================================

    public static class PermissoesResponse {
        private Boolean podeCreiarExcursoes;
        private Boolean podeGerenciarUsuarios;
        private Boolean podeVerFinanceiro;
        private Boolean podeEditarCompania;
        private Boolean podeEnviarNotificacoes;
        private String roleCompania;

        // Getters e Setters
        public Boolean getPodeCreiarExcursoes() { return podeCreiarExcursoes; }
        public void setPodeCreiarExcursoes(Boolean podeCreiarExcursoes) { this.podeCreiarExcursoes = podeCreiarExcursoes; }

        public Boolean getPodeGerenciarUsuarios() { return podeGerenciarUsuarios; }
        public void setPodeGerenciarUsuarios(Boolean podeGerenciarUsuarios) { this.podeGerenciarUsuarios = podeGerenciarUsuarios; }

        public Boolean getPodeVerFinanceiro() { return podeVerFinanceiro; }
        public void setPodeVerFinanceiro(Boolean podeVerFinanceiro) { this.podeVerFinanceiro = podeVerFinanceiro; }

        public Boolean getPodeEditarCompania() { return podeEditarCompania; }
        public void setPodeEditarCompania(Boolean podeEditarCompania) { this.podeEditarCompania = podeEditarCompania; }

        public Boolean getPodeEnviarNotificacoes() { return podeEnviarNotificacoes; }
        public void setPodeEnviarNotificacoes(Boolean podeEnviarNotificacoes) { this.podeEnviarNotificacoes = podeEnviarNotificacoes; }

        public String getRoleCompania() { return roleCompania; }
        public void setRoleCompania(String roleCompania) { this.roleCompania = roleCompania; }
    }
}