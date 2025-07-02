package br.com.tourapp.service;

import br.com.tourapp.dto.request.InscricaoRequest;
import br.com.tourapp.dto.response.InscricaoResponse;
import br.com.tourapp.entity.Cliente;
import br.com.tourapp.entity.Excursao;
import br.com.tourapp.entity.Inscricao;
import br.com.tourapp.enums.StatusExcursao;
import br.com.tourapp.enums.StatusPagamento;
import br.com.tourapp.exception.BusinessException;
import br.com.tourapp.exception.NotFoundException;
import br.com.tourapp.repository.InscricaoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final ExcursaoService excursaoService;
    private final ClienteService clienteService;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    public InscricaoService(InscricaoRepository inscricaoRepository,
                            ExcursaoService excursaoService,
                            ClienteService clienteService,
                            EmailService emailService,
                            ModelMapper modelMapper) {
        this.inscricaoRepository = inscricaoRepository;
        this.excursaoService = excursaoService;
        this.clienteService = clienteService;
        this.emailService = emailService;
        this.modelMapper = modelMapper;
    }

    public InscricaoResponse criarInscricao(UUID excursaoId, InscricaoRequest request, UUID clienteId) {
        Excursao excursao = excursaoService.obterPorId(excursaoId);
        Cliente cliente = clienteService.obterPorId(clienteId);

        // Validações
        if (!excursao.isAtiva()) {
            throw new BusinessException("Excursão não está disponível para inscrições");
        }

        if (!excursao.temVagasDisponiveis()) {
            throw new BusinessException("Excursão está lotada");
        }

        if (inscricaoRepository.existsByClienteIdAndExcursaoId(clienteId, excursaoId)) {
            throw new BusinessException("Cliente já está inscrito nesta excursão");
        }

        // Criar inscrição
        Inscricao inscricao = new Inscricao();
        inscricao.setExcursao(excursao);
        inscricao.setCliente(cliente);
        inscricao.setValorPago(excursao.getPreco());
        inscricao.setObservacoesCliente(request.getObservacoesCliente());
        inscricao.setStatusPagamento(StatusPagamento.PENDENTE);

        inscricao = inscricaoRepository.save(inscricao);

        // Atualizar vagas ocupadas
        excursao.setVagasOcupadas(excursao.getVagasOcupadas() + 1);
        if (excursao.getVagasOcupadas().equals(excursao.getVagasTotal())) {
            excursao.setStatus(StatusExcursao.LOTADA);
        }

        // Enviar email de confirmação
        emailService.enviarConfirmacaoInscricao(inscricao);

        return converterParaResponse(inscricao);
    }

    @Transactional(readOnly = true)
    public Page<InscricaoResponse> listarInscricoesPorCliente(UUID clienteId, Pageable pageable) {
        Page<Inscricao> inscricoes = inscricaoRepository.findByClienteId(clienteId, pageable);
        return inscricoes.map(this::converterParaResponse);
    }

    @Transactional(readOnly = true)
    public InscricaoResponse obterInscricaoPorCliente(UUID inscricaoId, UUID clienteId) {
        Inscricao inscricao = inscricaoRepository.findByIdAndClienteId(inscricaoId, clienteId)
                .orElseThrow(() -> new NotFoundException("Inscrição não encontrada"));

        return converterParaResponse(inscricao);
    }

    @Transactional(readOnly = true)
    public Page<InscricaoResponse> listarInscricoesPorOrganizador(UUID organizadorId, UUID excursaoId, Pageable pageable) {
        return inscricaoRepository.findByOrganizadorIdAndExcursaoId(organizadorId, excursaoId, pageable)
                .map(this::converterParaResponse);
    }

    @Transactional(readOnly = true)
    public Page<InscricaoResponse> listarInscricoesPorExcursao(UUID excursaoId, UUID organizadorId, Pageable pageable) {
        // Verificar se a excursão pertence ao organizador
        excursaoService.obterExcursaoPorOrganizador(excursaoId, organizadorId);

        Page<Inscricao> inscricoes = inscricaoRepository.findByExcursaoId(excursaoId, pageable);
        return inscricoes.map(this::converterParaResponse);
    }

    @Transactional(readOnly = true)
    public Inscricao obterPorId(UUID inscricaoId) {
        return inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new NotFoundException("Inscrição não encontrada"));
    }

    public void atualizarStatusPagamento(UUID inscricaoId, StatusPagamento novoStatus) {
        Inscricao inscricao = obterPorId(inscricaoId);
        inscricao.setStatusPagamento(novoStatus);
        inscricaoRepository.save(inscricao);

        // Enviar notificação se aprovado
        if (novoStatus == StatusPagamento.APROVADO) {
            emailService.enviarConfirmacaoPagamento(inscricao);
        }
    }

    private InscricaoResponse converterParaResponse(Inscricao inscricao) {
        InscricaoResponse response = modelMapper.map(inscricao, InscricaoResponse.class);
        response.setTituloExcursao(inscricao.getExcursao().getTitulo());
        response.setDataSaidaExcursao(inscricao.getExcursao().getDataSaida());
        response.setNomeCliente(inscricao.getCliente().getNome());
        response.setEmailCliente(inscricao.getCliente().getEmail());
        response.setTelefoneCliente(inscricao.getCliente().getTelefone());
        return response;
    }
}
