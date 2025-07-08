package br.com.tourapp.service;

import br.com.tourapp.dto.request.NotificacaoRequest;
import br.com.tourapp.dto.response.NotificacaoResponse;
import br.com.tourapp.dto.response.UserInfoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationUseCase {
    NotificacaoResponse criarNotificacao(@Valid NotificacaoRequest request, UUID id);

    void enviarNotificacao(UUID id, UUID id1);

    Page<NotificacaoResponse> listarNotificacoesPorOrganizador(UUID id, Pageable pageable);

    List<UserInfoResponse> listarClientesPorExcursao(UUID excursaoId, UUID id);
}
