package br.com.tourapp.service;

import br.com.tourapp.controller.OrganizadorController;
import br.com.tourapp.dto.request.UpdateUserRequest;
import br.com.tourapp.dto.response.DashboardResponse;
import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.UserInfoResponse;
import br.com.tourapp.entity.UserEntity;
import br.com.tourapp.dto.SecurityUser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface UserUseCase {
    SecurityUser loadSecurityUserByEmail(String email);

    UserInfoResponse getUserInfo(String username);

    Map<String, Object> checkSubscription(String username);

    void updateSubscription(String email, String plan, int months);

    UserService.Pair<UserEntity, UserDetails> processGoogleToken(String googleToken);

    JwtResponse buildJwtResponse(UserEntity user, String accessToken, String token);

    UserInfoResponse obterPerfil(UUID id);

    UserInfoResponse atualizarPerfil(UUID id, UpdateUserRequest request);

    DashboardResponse obterDashboard(UUID id, UUID companiaId, LocalDate dataInicio, LocalDate dataFim);

    DashboardResponse obterDashboardConsolidado(UUID id, LocalDate dataInicio, LocalDate dataFim);

    UserEntity obterPorId(UUID clienteId);

    void atualizarPushToken(UUID id, String pushToken);

    void atualizarConfiguracoes(UUID id, Boolean emailNotifications, Boolean smsNotifications);

    OrganizadorController.ReceitaEstatisticasResponse obterEstatisticasReceita(UUID id, UUID companiaId, LocalDate dataInicio, LocalDate dataFim);

    OrganizadorController.ExcursoesEstatisticasResponse obterEstatisticasExcursoes(UUID id, UUID companiaId, LocalDate dataInicio, LocalDate dataFim);

    OrganizadorController.RelatorioVendasResponse gerarRelatorioVendas(UUID id, UUID companiaId, LocalDate dataInicio, LocalDate dataFim, Boolean incluirDetalhado);
}
