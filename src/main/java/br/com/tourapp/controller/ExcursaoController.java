package br.com.tourapp.controller;

import br.com.tourapp.dto.request.ExcursaoRequest;
import br.com.tourapp.dto.response.ExcursaoResponse;
import br.com.tourapp.enums.StatusExcursao;
import br.com.tourapp.dto.SecurityUser;
import br.com.tourapp.service.ExcursaoService;
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
public class ExcursaoController {

    private final ExcursaoService excursaoService;

    public ExcursaoController(ExcursaoService excursaoService) {
        this.excursaoService = excursaoService;
    }

    @PostMapping
    public ResponseEntity<ExcursaoResponse> criarExcursao(
            @Valid @ModelAttribute ExcursaoRequest request,
            @AuthenticationPrincipal SecurityUser user) {
        ExcursaoResponse response = excursaoService.criarExcursao(request, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ExcursaoResponse>> listarExcursoes(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) StatusExcursao status,
            Pageable pageable) {
        Page<ExcursaoResponse> response = excursaoService.listarExcursoesPorOrganizador(
                user.getId(), status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExcursaoResponse> obterExcursao(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {
        ExcursaoResponse response = excursaoService.obterExcursaoPorOrganizador(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExcursaoResponse> atualizarExcursao(
            @PathVariable UUID id,
            @Valid @ModelAttribute ExcursaoRequest request,
            @AuthenticationPrincipal SecurityUser user) {
        ExcursaoResponse response = excursaoService.atualizarExcursao(id, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ExcursaoResponse> alterarStatus(
            @PathVariable UUID id,
            @RequestParam StatusExcursao status,
            @AuthenticationPrincipal SecurityUser user) {
        ExcursaoResponse response = excursaoService.alterarStatusExcursao(id, status, user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirExcursao(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {
        excursaoService.excluirExcursao(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

