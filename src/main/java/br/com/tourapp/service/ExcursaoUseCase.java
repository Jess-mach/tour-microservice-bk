package br.com.tourapp.service;

import br.com.tourapp.controller.ExcursaoController;
import br.com.tourapp.dto.request.ExcursaoRequest;
import br.com.tourapp.dto.response.ExcursaoResponse;
import br.com.tourapp.enums.StatusExcursao;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ExcursaoUseCase {
    ExcursaoResponse criarExcursao(@Valid ExcursaoRequest request, UUID companiaId, UUID id);

    Page<ExcursaoResponse> listarExcursoesPorOrganizador(UUID id, UUID companiaId, StatusExcursao status, Pageable pageable);

    ExcursaoResponse obterExcursaoPorOrganizador(UUID id, UUID id1);

    ExcursaoResponse atualizarExcursao(UUID id, @Valid ExcursaoRequest request, UUID id1);

    ExcursaoResponse alterarStatusExcursao(UUID id, StatusExcursao status, UUID id1);

    void excluirExcursao(UUID id, UUID id1);

    Page<ExcursaoResponse> listarExcursoesPorCompania(UUID companiaId, StatusExcursao status, Pageable pageable);

    ExcursaoController.LoteOperacaoResponse alterarStatusLote(List<UUID> excursaoIds, StatusExcursao novoStatus, UUID id);

    ExcursaoController.LoteOperacaoResponse excluirExcursoesLote(List<UUID> excursaoIds, UUID id);

    ExcursaoController.ExcursaoEstatisticasResponse obterEstatisticasExcursao(UUID id, UUID id1);

    ExcursaoController.ResumoEstatisticasResponse obterResumoEstatisticas(UUID id, UUID companiaId);

    ExcursaoResponse duplicarExcursao(UUID id, ExcursaoController.DuplicarExcursaoRequest request, UUID id1);

    ExcursaoController.TemplateResponse salvarComoTemplate(UUID id, ExcursaoController.SalvarTemplateRequest request, UUID id1);
}
