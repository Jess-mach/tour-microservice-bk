package br.com.tourapp.controller;

import br.com.tourapp.dto.request.NotificacaoRequest;
import br.com.tourapp.dto.response.NotificacaoResponse;
import br.com.tourapp.dto.response.UserInfoResponse;
import br.com.tourapp.service.NotificationUseCase;
import br.com.tourapp.dto.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organizador/notificacoes")
@PreAuthorize("hasRole('ORGANIZADOR')")
public class NotificacaoController {

    private final NotificationUseCase notificacaoService;

    public NotificacaoController(NotificationUseCase notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @PostMapping
    public ResponseEntity<NotificacaoResponse> criarNotificacao(
            @Valid @RequestBody NotificacaoRequest request,
            @AuthenticationPrincipal SecurityUser user) {
        NotificacaoResponse response = notificacaoService.criarNotificacao(request, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<Void> enviarNotificacao(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {
        notificacaoService.enviarNotificacao(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<NotificacaoResponse>> listarNotificacoes(
            @AuthenticationPrincipal SecurityUser user,
            Pageable pageable) {
        Page<NotificacaoResponse> response = notificacaoService.listarNotificacoesPorOrganizador(user.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clientes/{excursaoId}")
    public ResponseEntity<List<UserInfoResponse>> listarClientesPorExcursao(
            @PathVariable UUID excursaoId,
            @AuthenticationPrincipal SecurityUser user) {
        List<UserInfoResponse> response = notificacaoService.listarClientesPorExcursao(excursaoId, user.getId());
        return ResponseEntity.ok(response);
    }
}

