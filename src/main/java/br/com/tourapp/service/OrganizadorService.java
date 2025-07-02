package br.com.tourapp.service;

import br.com.tourapp.dto.response.DashboardResponse;
import br.com.tourapp.dto.response.OrganizadorResponse;
import br.com.tourapp.entity.Organizador;
import br.com.tourapp.enums.StatusExcursao;
import br.com.tourapp.exception.NotFoundException;
import br.com.tourapp.repository.ExcursaoRepository;
import br.com.tourapp.repository.InscricaoRepository;
import br.com.tourapp.repository.OrganizadorRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class OrganizadorService {

    private final OrganizadorRepository organizadorRepository;
    private final ExcursaoRepository excursaoRepository;
    private final InscricaoRepository inscricaoRepository;
    private final ModelMapper modelMapper;

    public OrganizadorService(OrganizadorRepository organizadorRepository,
                              ExcursaoRepository excursaoRepository,
                              InscricaoRepository inscricaoRepository,
                              ModelMapper modelMapper) {
        this.organizadorRepository = organizadorRepository;
        this.excursaoRepository = excursaoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    public OrganizadorResponse obterPerfil(UUID organizadorId) {
        Organizador organizador = organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new NotFoundException("Organizador não encontrado"));

        return modelMapper.map(organizador, OrganizadorResponse.class);
    }

    public OrganizadorResponse atualizarPerfil(UUID organizadorId, OrganizadorResponse request) {
        Organizador organizador = organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new NotFoundException("Organizador não encontrado"));

        organizador.setNomeEmpresa(request.getNomeEmpresa());
        organizador.setNomeResponsavel(request.getNomeResponsavel());
        organizador.setTelefone(request.getTelefone());
        organizador.setPixKey(request.getPixKey());
        organizador.setCnpj(request.getCnpj());

        organizador = organizadorRepository.save(organizador);
        return modelMapper.map(organizador, OrganizadorResponse.class);
    }

    @Transactional(readOnly = true)
    public DashboardResponse obterDashboard(UUID organizadorId, LocalDate dataInicio, LocalDate dataFim) {
        // Se não informar datas, usar o mês atual
        if (dataInicio == null) {
            dataInicio = LocalDate.now().withDayOfMonth(1);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }

        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);

        // Buscar métricas
        Long totalExcursoes = excursaoRepository.countByOrganizadorIdAndStatus(organizadorId, StatusExcursao.ATIVA);

        BigDecimal receitaTotal = inscricaoRepository.findTotalReceitaByOrganizadorIdAndPeriodo(
                organizadorId, inicio, fim);

        if (receitaTotal == null) {
            receitaTotal = BigDecimal.ZERO;
        }

        var excursoesPeriodo = excursaoRepository.findByOrganizadorIdAndDataSaidaBetween(
                organizadorId, inicio, fim);

        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setTotalExcursoes(totalExcursoes);
        dashboard.setReceitaTotal(receitaTotal);
        dashboard.setExcursoesMes((long) excursoesPeriodo.size());
        dashboard.setPeriodoInicio(dataInicio);
        dashboard.setPeriodoFim(dataFim);

        return dashboard;
    }

    @Transactional(readOnly = true)
    public Organizador obterPorId(UUID organizadorId) {
        return organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new NotFoundException("Organizador não encontrado"));
    }
}
