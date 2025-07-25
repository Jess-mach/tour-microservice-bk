package br.com.tourapp.controller;

import br.com.tourapp.dto.request.UpdateUserRequest;
import br.com.tourapp.dto.response.InscricaoResponse;
import br.com.tourapp.dto.response.UserInfoResponse;
import br.com.tourapp.service.InscricaoService;
import br.com.tourapp.dto.SecurityUser;
import br.com.tourapp.service.UserUseCase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cliente")
@PreAuthorize("hasRole('CLIENTE')")
public class ClienteController {

    private final UserUseCase clienteService;
    private final InscricaoService inscricaoService;

    public ClienteController(UserUseCase clienteService, InscricaoService inscricaoService) {
        this.clienteService = clienteService;
        this.inscricaoService = inscricaoService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<UserInfoResponse> obterPerfil(@AuthenticationPrincipal SecurityUser user) {
        UserInfoResponse response = clienteService.obterPerfil(user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/perfil")
    public ResponseEntity<UserInfoResponse> atualizarPerfil(
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal SecurityUser user) {
        UserInfoResponse response = clienteService.atualizarPerfil(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inscricoes")
    public ResponseEntity<Page<InscricaoResponse>> listarInscricoes(
            @AuthenticationPrincipal SecurityUser user,
            Pageable pageable) {
        Page<InscricaoResponse> response = inscricaoService.listarInscricoesPorCliente(user.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inscricoes/{id}")
    public ResponseEntity<InscricaoResponse> obterInscricao(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {
        InscricaoResponse response = inscricaoService.obterInscricaoPorCliente(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/notificacoes/push-token")
    public ResponseEntity<Void> atualizarPushToken(
            @RequestParam String pushToken,
            @AuthenticationPrincipal SecurityUser user) {
        clienteService.atualizarPushToken(user.getId(), pushToken);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notificacoes/configuracoes")
    public ResponseEntity<Void> atualizarConfiguracoes(
            @RequestParam Boolean emailNotifications,
            @RequestParam Boolean smsNotifications,
            @AuthenticationPrincipal SecurityUser user) {
        clienteService.atualizarConfiguracoes(user.getId(), emailNotifications, smsNotifications);
        return ResponseEntity.ok().build();
    }
}

