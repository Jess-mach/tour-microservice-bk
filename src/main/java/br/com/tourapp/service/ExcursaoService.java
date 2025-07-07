package br.com.tourapp.service;

import br.com.tourapp.dto.request.ExcursaoRequest;
import br.com.tourapp.dto.response.ExcursaoResponse;
import br.com.tourapp.entity.Excursao;
import br.com.tourapp.entity.CompaniaEntity;
import br.com.tourapp.entity.UserEntity;
import br.com.tourapp.enums.StatusExcursao;
import br.com.tourapp.exception.BusinessException;
import br.com.tourapp.exception.NotFoundException;
import br.com.tourapp.repository.ExcursaoRepository;
import br.com.tourapp.repository.CompaniaRepository;
import br.com.tourapp.repository.UserRepository;
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
    private final CompaniaRepository companiaRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ModelMapper modelMapper;

    public ExcursaoService(ExcursaoRepository excursaoRepository,
                           CompaniaRepository companiaRepository,
                           UserRepository userRepository,
                           S3Service s3Service,
                           ModelMapper modelMapper) {
        this.excursaoRepository = excursaoRepository;
        this.companiaRepository = companiaRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
        this.modelMapper = modelMapper;
    }

    @CacheEvict(value = "excursoes", allEntries = true)
    public ExcursaoResponse criarExcursao(ExcursaoRequest request, UUID companiaId, UUID criadorId) {
        CompaniaEntity compania = companiaRepository.findById(companiaId)
                .orElseThrow(() -> new NotFoundException("Compania não encontrada"));

        UserEntity criador = userRepository.findById(criadorId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

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
        excursao.setCompania(compania);
        excursao.setCriador(criador);

        // Upload de imagens
        if (request.getImagens() != null && !request.getImagens().isEmpty()) {
            List<String> urlsImagens = s3Service.uploadMultiplas(request.getImagens());
            excursao.setImagens(urlsImagens);
        }

        excursao = excursaoRepository.save(excursao);
        return converterParaResponse(excursao);
    }

    // Método para compatibilidade (usando organizadorId = userId)
    @CacheEvict(value = "excursoes", allEntries = true)
    public ExcursaoResponse criarExcursao(ExcursaoRequest request, UUID organizadorId) {
        // Para compatibilidade, assumir que organizadorId é o criadorId
        // e buscar a primeira compania do usuário
        UserEntity criador = userRepository.findById(organizadorId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        List<CompaniaEntity> companias = companiaRepository.findByUserId(organizadorId);
        if (companias.isEmpty()) {
            throw new BusinessException("Usuário não possui nenhuma compania");
        }

        CompaniaEntity compania = companias.get(0); // Usar primeira compania
        return criarExcursao(request, compania.getId(), organizadorId);
    }

    @Transactional(readOnly = true)
    public Page<ExcursaoResponse> listarExcursoesPorCompania(UUID companiaId, StatusExcursao status, Pageable pageable) {
        Page<Excursao> excursoes;

        if (status != null) {
            excursoes = excursaoRepository.findByCompaniaIdAndStatus(companiaId, status, pageable);
        } else {
            excursoes = excursaoRepository.findByCompaniaId(companiaId, pageable);
        }

        return excursoes.map(this::converterParaResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "excursoes", key = "#organizadorId + '_' + #status + '_' + #pageable.pageNumber")
    public Page<ExcursaoResponse> listarExcursoesPorOrganizador(UUID organizadorId, StatusExcursao status, Pageable pageable) {
        // Buscar excursões de todas as companias do usuário
        List<CompaniaEntity> companias = companiaRepository.findByUserId(organizadorId);
        if (companias.isEmpty()) {
            return Page.empty(pageable);
        }

        // Por simplicidade, buscar da primeira compania (pode ser expandido para buscar de todas)
        UUID companiaId = companias.get(0).getId();
        return listarExcursoesPorCompania(companiaId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ExcursaoResponse> listarExcursoesPorOrganizador(UUID organizadorId, UUID companiaId, StatusExcursao status, Pageable pageable) {
        if (companiaId != null) {
            return listarExcursoesPorCompania(companiaId, status, pageable);
        } else {
            return listarExcursoesPorOrganizador(organizadorId, status, pageable);
        }
    }

    @Transactional(readOnly = true)
    public ExcursaoResponse obterExcursaoPorOrganizador(UUID excursaoId, UUID organizadorId) {
        Excursao excursao = excursaoRepository.findById(excursaoId)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada"));

        // Verificar se o usuário tem acesso a esta excursão (via compania)
        List<CompaniaEntity> companias = companiaRepository.findByUserId(organizadorId);
        boolean temAcesso = companias.stream()
                .anyMatch(c -> c.getId().equals(excursao.getCompania().getId()));

        if (!temAcesso) {
            throw new NotFoundException("Excursão não encontrada");
        }

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
        Excursao excursao = excursaoRepository.findById(excursaoId)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada"));

        // Verificar se o usuário tem acesso a esta excursão
        List<CompaniaEntity> companias = companiaRepository.findByUserId(organizadorId);
        boolean temAcesso = companias.stream()
                .anyMatch(c -> c.getId().equals(excursao.getCompania().getId()));

        if (!temAcesso) {
            throw new NotFoundException("Excursão não encontrada");
        }

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
        Excursao excursao = excursaoRepository.findById(excursaoId)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada"));

        // Verificar se o usuário tem acesso a esta excursão
        List<CompaniaEntity> companias = companiaRepository.findByUserId(organizadorId);
        boolean temAcesso = companias.stream()
                .anyMatch(c -> c.getId().equals(excursao.getCompania().getId()));

        if (!temAcesso) {
            throw new NotFoundException("Excursão não encontrada");
        }

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
        Excursao excursao = excursaoRepository.findById(excursaoId)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada"));

        // Verificar se o usuário tem acesso a esta excursão
        List<CompaniaEntity> companias = companiaRepository.findByUserId(organizadorId);
        boolean temAcesso = companias.stream()
                .anyMatch(c -> c.getId().equals(excursao.getCompania().getId()));

        if (!temAcesso) {
            throw new NotFoundException("Excursão não encontrada");
        }

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

        // Usar dados da compania ao invés do organizador
        response.setNomeOrganizador(excursao.getCompania().getNomeEmpresa());
        response.setEmailOrganizador(excursao.getCriador().getEmail());
        response.setTelefoneOrganizador(excursao.getCriador().getPhone());

        // Dados da compania
        response.setCompaniaId(excursao.getCompania().getId());
        response.setNomeCompania(excursao.getCompania().getNomeEmpresa());
        response.setCriadorId(excursao.getCriador().getId());
        response.setNomeCriador(excursao.getCriador().getFullName());

        return response;
    }
}