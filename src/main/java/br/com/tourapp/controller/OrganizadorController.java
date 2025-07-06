package br.com.tourapp.controller;

import br.com.tourapp.dto.response.DashboardResponse;
import br.com.tourapp.dto.response.InscricaoResponse;
import br.com.tourapp.dto.response.OrganizadorResponse;
import br.com.tourapp.service.OrganizadorService;
import br.com.tourapp.service.InscricaoService;
import br.com.tourapp.dto.SecurityUser;
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
public class OrganizadorController {

    private final OrganizadorService organizadorService;
    private final InscricaoService inscricaoService;

    public OrganizadorController(OrganizadorService organizadorService, InscricaoService inscricaoService) {
        this.organizadorService = organizadorService;
        this.inscricaoService = inscricaoService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<OrganizadorResponse> obterPerfil(@AuthenticationPrincipal SecurityUser user) {
        OrganizadorResponse response = organizadorService.obterPerfil(user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/perfil")
    public ResponseEntity<OrganizadorResponse> atualizarPerfil(
            @RequestBody OrganizadorResponse request,
            @AuthenticationPrincipal SecurityUser user) {
        OrganizadorResponse response = organizadorService.atualizarPerfil(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> obterDashboard(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        DashboardResponse response = organizadorService.obterDashboard(user.getId(), dataInicio, dataFim);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inscricoes")
    public ResponseEntity<Page<InscricaoResponse>> listarTodasInscricoes(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) UUID excursaoId,
            Pageable pageable) {
        Page<InscricaoResponse> response = inscricaoService.listarInscricoesPorOrganizador(
                user.getId(), excursaoId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/excursoes/{excursaoId}/inscricoes")
    public ResponseEntity<Page<InscricaoResponse>> listarInscricoesPorExcursao(
            @PathVariable UUID excursaoId,
            @AuthenticationPrincipal SecurityUser user,
            Pageable pageable) {
        Page<InscricaoResponse> response = inscricaoService.listarInscricoesPorExcursao(
                excursaoId, user.getId(), pageable);
        return ResponseEntity.ok(response);
    }
}

