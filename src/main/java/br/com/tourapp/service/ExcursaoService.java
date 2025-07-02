package br.com.tourapp.service;

import br.com.tourapp.dto.request.ExcursaoRequest;
import br.com.tourapp.dto.response.ExcursaoResponse;
import br.com.tourapp.entity.Excursao;
import br.com.tourapp.entity.Organizador;
import br.com.tourapp.enums.StatusExcursao;
import br.com.tourapp.exception.BusinessException;
import br.com.tourapp.exception.NotFoundException;
import br.com.tourapp.repository.ExcursaoRepository;
import br.com.tourapp.repository.OrganizadorRepository;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExcursaoService {

    private final ExcursaoRepository excursaoRepository;
    private final OrganizadorRepository organizadorRepository;
    private final S3Service s3Service;
    private final ModelMapper modelMapper;

    public ExcursaoService(ExcursaoRepository excursaoRepository,
                           OrganizadorRepository organizadorRepository,
                           S3Service s3Service,
                           ModelMapper modelMapper) {
        this.excursaoRepository = excursaoRepository;
        this.organizadorRepository = organizadorRepository;
        this.s3Service = s3Service;
        this.modelMapper = modelMapper;
    }

    @CacheEvict(value = "excursoes", allEntries = true)
    public ExcursaoResponse criarExcursao(ExcursaoRequest request, UUID organizadorId) {
        Organizador organizador = organizadorRepository.findById(organizadorId)
                .orElseThrow(() -> new NotFoundException("Organizador não encontrado"));

        Excursao excursao = new Excursao();
        excursao.setTitulo(request.getTitulo());
        excursao.setDescricao(request.getDescricao());
        excursao.setDataSaida(request.getDataSaida());
        excursao.setDataRetorno(request.getDataRetorno());
        excursao.setPreco(request.getPreco());
        excursao.setVagasTotal(request.getVagasTotal());
        excursao.setLocalSaida(request.getLocalSaida());
        excursao.setLocalDestino(request.getLocalDestino());
        excursao.setObservacoes(request.getObservacoes());
        excursao.setAceitaPix(request.getAceitaPix());
        excursao.setAceitaCartao(request.getAceitaCartao());
        excursao.setOrganizador(organizador);

        // Upload de imagens
        if (request.getImagens() != null && !request.getImagens().isEmpty()) {
            List<String> urlsImagens = s3Service.uploadMultiplas(request.getImagens());
            excursao.setImagens(urlsImagens);
        }

        excursao = excursaoRepository.save(excursao);
        return converterParaResponse(excursao);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "excursoes", key = "#organizadorId + '_' + #status + '_' + #pageable.pageNumber")
    public Page<ExcursaoResponse> listarExcursoesPorOrganizador(UUID organizadorId, StatusExcursao status, Pageable pageable) {
        Page<Excursao> excursoes;

        if (status != null) {
            excursoes = excursaoRepository.findByOrganizadorIdAndStatus(organizadorId, status, pageable);
        } else {
            excursoes = excursaoRepository.findByOrganizadorId(organizadorId, pageable);
        }

        return excursoes.map(this::converterParaResponse);
    }

    @Transactional(readOnly = true)
    public ExcursaoResponse obterExcursaoPorOrganizador(UUID excursaoId, UUID organizadorId) {
        Excursao excursao = excursaoRepository.findByIdAndOrganizadorId(excursaoId, organizadorId)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada"));

        return converterParaResponse(excursao);
    }

    @Transactional(readOnly = true)
    public ExcursaoResponse obterExcursaoPublica(UUID excursaoId) {
        Excursao excursao = excursaoRepository.findById(excursaoId)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada"));

        if (!excursao.isAtiva()) {
            throw new BusinessException("Excursão não está disponível para inscrições");
        }

        return converterParaResponse(excursao);
    }

    @CacheEvict(value = "excursoes", allEntries = true)
    public ExcursaoResponse atualizarExcursao(UUID excursaoId, ExcursaoRequest request, UUID organizadorId) {
        Excursao excursao = excursaoRepository.findByIdAndOrganizadorId(excursaoId, organizadorId)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada"));

        // Validar se pode ser editada
        if (excursao.getVagasOcupadas() > 0 && !request.getVagasTotal().equals(excursao.getVagasTotal())) {
            if (request.getVagasTotal() < excursao.getVagasOcupadas()) {
                throw new BusinessException("Não é possível reduzir vagas abaixo do número de inscritos");
            }
        }

        excursao.setTitulo(request.getTitulo());
        excursao.setDescricao(request.getDescricao());
        excursao.setDataSaida(request.getDataSaida());
        excursao.setDataRetorno(request.getDataRetorno());
        excursao.setPreco(request.getPreco());
        excursao.setVagasTotal(request.getVagasTotal());
        excursao.setLocalSaida(request.getLocalSaida());
        excursao.setLocalDestino(request.getLocalDestino());
        excursao.setObservacoes(request.getObservacoes());
        excursao.setAceitaPix(request.getAceitaPix());
        excursao.setAceitaCartao(request.getAceitaCartao());

        // Upload de novas imagens se fornecidas
        if (request.getImagens() != null && !request.getImagens().isEmpty()) {
            List<String> urlsImagens = s3Service.uploadMultiplas(request.getImagens());
            excursao.setImagens(urlsImagens);
        }

        excursao = excursaoRepository.save(excursao);
        return converterParaResponse(excursao);
    }

    @CacheEvict(value = "excursoes", allEntries = true)
    public ExcursaoResponse alterarStatusExcursao(UUID excursaoId, StatusExcursao novoStatus, UUID organizadorId) {
        Excursao excursao = excursaoRepository.findByIdAndOrganizadorId(excursaoId, organizadorId)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada"));

        // Validações de negócio
        if (novoStatus == StatusExcursao.ATIVA && excursao.getDataSaida().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível ativar excursão com data de saída no passado");
        }

        excursao.setStatus(novoStatus);
        excursao = excursaoRepository.save(excursao);

        return converterParaResponse(excursao);
    }

    @CacheEvict(value = "excursoes", allEntries = true)
    public void excluirExcursao(UUID excursaoId, UUID organizadorId) {
        Excursao excursao = excursaoRepository.findByIdAndOrganizadorId(excursaoId, organizadorId)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada"));

        if (excursao.getVagasOcupadas() > 0) {
            throw new BusinessException("Não é possível excluir excursão com inscrições");
        }

        excursaoRepository.delete(excursao);
    }

    @Transactional(readOnly = true)
    public Excursao obterPorId(UUID excursaoId) {
        return excursaoRepository.findById(excursaoId)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada"));
    }

    private ExcursaoResponse converterParaResponse(Excursao excursao) {
        ExcursaoResponse response = modelMapper.map(excursao, ExcursaoResponse.class);
        response.setVagasDisponiveis(excursao.getVagasDisponiveis());
        response.setNomeOrganizador(excursao.getOrganizador().getNomeEmpresa());
        response.setEmailOrganizador(excursao.getOrganizador().getEmail());
        response.setTelefoneOrganizador(excursao.getOrganizador().getTelefone());
        return response;
    }
}