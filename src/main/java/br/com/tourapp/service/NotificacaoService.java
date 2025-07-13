//src/main/java/br/com/tourapp/service/NotificacaoService.java
package br.com.tourapp.service;

import br.com.tourapp.dto.request.NotificacaoRequest;
import br.com.tourapp.dto.response.NotificacaoResponse;
import br.com.tourapp.dto.response.UserInfoResponse;
import br.com.tourapp.entity.CompaniaEntity;
import br.com.tourapp.entity.Excursao;
import br.com.tourapp.entity.Notificacao;
import br.com.tourapp.entity.UserEntity;
import br.com.tourapp.enums.TipoNotificacao;
import br.com.tourapp.exception.BusinessException;
import br.com.tourapp.exception.NotFoundException;
import br.com.tourapp.repository.CompaniaRepository;
import br.com.tourapp.repository.NotificacaoRepository;
import br.com.tourapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificacaoService implements NotificationUseCase {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);

    private final NotificacaoRepository notificacaoRepository;
    private final ExcursaoService excursaoService;
    private final UserRepository userRepository; // AJUSTADO
    private final EmailService emailService;
    private final FirebaseService firebaseService;
    private final ModelMapper modelMapper;
    private final CompaniaRepository companiaRepository; // ADICIONADO

    @Override
    public NotificacaoResponse criarNotificacao(NotificacaoRequest request, UUID organizadorId) {
        logger.info("Criando notificação para organizador: {}", organizadorId);

        // AJUSTADO - buscar usuário e primeira compania
        UserEntity organizador = userRepository.findById(organizadorId)
                .orElseThrow(() -> new NotFoundException("Organizador não encontrado"));

        List<CompaniaEntity> companias = companiaRepository.findByUserId(organizadorId);
        if (companias.isEmpty()) {
            throw new BusinessException("Usuário não possui nenhuma compania");
        }
        CompaniaEntity compania = companias.get(0); // Usar primeira compania

        // Validações de negócio
        validarNotificacao(request, organizadorId);

        Notificacao notificacao = new Notificacao();
        notificacao.setCompania(compania); // AJUSTADO
        notificacao.setOrganizador(organizador);
        notificacao.setTitulo(request.getTitulo());
        notificacao.setMensagem(request.getMensagem());
        notificacao.setTipo(request.getTipo() != null ? request.getTipo() : TipoNotificacao.INFO);
        notificacao.setEnviarParaTodos(request.getEnviarParaTodos());

        // Se especificou excursão, vincular e validar
        if (request.getExcursaoId() != null) {
            Excursao excursao = modelMapper.map(
                    excursaoService.obterExcursaoPorOrganizador(request.getExcursaoId(), organizadorId), Excursao.class);

            notificacao.setExcursao(excursao);

            if (request.getEnviarParaTodos()) {
                notificacao.setEnviarParaTodos(false);
                logger.info("Ajustando notificação para enviar apenas para clientes da excursão: {}",
                        excursao.getTitulo());
            }
        }

        // Definir clientes alvo
        if (!notificacao.getEnviarParaTodos() && request.getClientesAlvo() != null && !request.getClientesAlvo().isEmpty()) {
            List<UserEntity> clientesValidos = userRepository.findAllById(request.getClientesAlvo());
            if (clientesValidos.size() != request.getClientesAlvo().size()) {
                throw new BusinessException("Alguns clientes especificados não foram encontrados");
            }
            notificacao.setClientesAlvo(request.getClientesAlvo());
        }

        notificacao = notificacaoRepository.save(notificacao);

        logger.info("Notificação criada com ID: {}", notificacao.getId());

        NotificacaoResponse response = converterParaResponse(notificacao);
        response.setTotalDestinatarios(calcularTotalDestinatarios(notificacao));

        return response;
    }

    @Transactional(readOnly = true)
    public List<UserInfoResponse> listarClientesPorExcursao(UUID excursaoId, UUID organizadorId) {
        logger.info("Listando clientes da excursão: {} do organizador: {}", excursaoId, organizadorId);

        // Verificar se a excursão pertence ao organizador
        excursaoService.obterExcursaoPorOrganizador(excursaoId, organizadorId);

        // AJUSTADO - buscar usuários inscritos na excursão
        return userRepository.findByExcursaoId(excursaoId)
                .stream()
                .map(cliente -> UserInfoResponse.builder()
                        .id(cliente.getId())
                        .email(cliente.getEmail())
                        .fullName(cliente.getFullName())
                        .profilePicture(cliente.getProfilePicture())
                        .createdAt(cliente.getCreatedAt())
                        .lastLogin(cliente.getLastLogin())
                        .build())
                .collect(Collectors.toList());
    }

    @Async
    public void enviarNotificacao(UUID notificacaoId, UUID organizadorId) {
        logger.info("Iniciando envio de notificação: {} do organizador: {}", notificacaoId, organizadorId);

        Notificacao notificacao = notificacaoRepository.findByIdAndOrganizadorId(notificacaoId, organizadorId);

        if (notificacao == null) {
            throw new NotFoundException("Notificação não encontrada");
        }

        if (notificacao.getEnviada()) {
            throw new BusinessException("Notificação já foi enviada em: " + notificacao.getEnviadaEm());
        }

        if (!notificacao.podeSerEnviada()) {
            throw new BusinessException("Notificação não pode ser enviada. Verifique os destinatários.");
        }

        try {
            // Obter lista de clientes destinatários
            List<UserEntity> destinatarios = obterDestinatarios(notificacao);

            if (destinatarios.isEmpty()) {
                throw new BusinessException("Nenhum cliente encontrado para enviar a notificação");
            }

            logger.info("Enviando notificação para {} clientes", destinatarios.size());

            // Enviar emails
            List<String> emailsValidos = filtrarEmailsValidos(destinatarios);
            if (!emailsValidos.isEmpty()) {
                emailService.enviarNotificacaoPersonalizada(notificacao, emailsValidos);
                logger.info("Emails enviados para {} destinatários", emailsValidos.size());
            }

            // Enviar push notifications
            List<String> pushTokensValidos = filtrarPushTokensValidos(destinatarios);
            if (!pushTokensValidos.isEmpty()) {
                firebaseService.enviarNotificacaoMultipla(
                        pushTokensValidos,
                        notificacao.getTitulo(),
                        notificacao.getMensagem()
                );
                logger.info("Push notifications enviadas para {} dispositivos", pushTokensValidos.size());
            }

            // Marcar como enviada
            notificacao.setEnviada(true);
            notificacao.setEnviadaEm(LocalDateTime.now());
            notificacaoRepository.save(notificacao);

            logger.info("Notificação {} enviada com sucesso para {} destinatários",
                    notificacao.getId(), destinatarios.size());

        } catch (Exception e) {
            logger.error("Erro ao enviar notificação {}: {}", notificacao.getId(), e.getMessage(), e);

            // Adicionar observação sobre o erro mas não marcar como enviada
            String observacao = "Erro no envio: " + e.getMessage();
            // Em uma implementação real, você poderia ter um campo para observações

            throw new BusinessException("Erro ao enviar notificação: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificacaoResponse> listarNotificacoesPorOrganizador(UUID organizadorId, Pageable pageable) {
        logger.info("Listando notificações do organizador: {}", organizadorId);

        Page<Notificacao> notificacoes = notificacaoRepository.findByOrganizadorId(organizadorId, pageable);

        return notificacoes.map(notificacao -> {
            NotificacaoResponse response = converterParaResponse(notificacao);
            response.setTotalDestinatarios(calcularTotalDestinatarios(notificacao));
            return response;
        });
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listarNotificacoesPendentes() {
        // Buscar notificações criadas há mais de 5 minutos que ainda não foram enviadas
        LocalDateTime tempoLimite = LocalDateTime.now().minusMinutes(5);
        return notificacaoRepository.findNotificacoesPendentes(tempoLimite);
    }

    @Transactional(readOnly = true)
    public Long contarNotificacoesEnviadas(UUID organizadorId) {
        return notificacaoRepository.countEnviadasByOrganizadorId(organizadorId);
    }

    // Métodos auxiliares privados

    private void validarNotificacao(NotificacaoRequest request, UUID organizadorId) {
        // Validar título e mensagem
        if (request.getTitulo() == null || request.getTitulo().trim().isEmpty()) {
            throw new BusinessException("Título da notificação é obrigatório");
        }

        if (request.getMensagem() == null || request.getMensagem().trim().isEmpty()) {
            throw new BusinessException("Mensagem da notificação é obrigatória");
        }

        // Validar destinatários
        if (!request.getEnviarParaTodos() &&
                (request.getClientesAlvo() == null || request.getClientesAlvo().isEmpty()) &&
                request.getExcursaoId() == null) {
            throw new BusinessException("É necessário especificar destinatários ou marcar 'enviar para todos'");
        }

        // Validar tipo de notificação
        if (request.getTipo() == TipoNotificacao.URGENTE) {
            // Notificações urgentes têm regras especiais
            Long notificacoesUrgentesHoje = contarNotificacoesUrgentesDoDia(organizadorId);
            if (notificacoesUrgentesHoje >= 3) {
                throw new BusinessException("Limite de 3 notificações urgentes por dia atingido");
            }
        }

        // Validar tamanho do título e mensagem
        if (request.getTitulo().length() > 100) {
            throw new BusinessException("Título não pode ter mais de 100 caracteres");
        }

        if (request.getMensagem().length() > 500) {
            throw new BusinessException("Mensagem não pode ter mais de 500 caracteres");
        }
    }

    private List<UserEntity> obterDestinatarios(Notificacao notificacao) {
        List<UserEntity> destinatarios = new ArrayList<>();

        if (notificacao.getEnviarParaTodos()) {
            if (notificacao.getExcursao() != null) {
                // Todos os clientes inscritos na excursão específica
                destinatarios = userRepository.findByExcursaoId(notificacao.getExcursao().getId());
            } else {
                // Todos os clientes ativos da plataforma
                destinatarios = userRepository.findByAtivoTrue();
            }
        } else if (notificacao.getExcursao() != null) {
            // Clientes da excursão específica
            destinatarios = userRepository.findByExcursaoId(notificacao.getExcursao().getId());
        } else if (notificacao.getClientesAlvo() != null && !notificacao.getClientesAlvo().isEmpty()) {
            // Clientes específicos
            destinatarios = userRepository.findAllById(notificacao.getClientesAlvo());
        }

        // Filtrar apenas clientes ativos
        return destinatarios.stream()
                .filter(UserEntity::isAtivo)
                .collect(Collectors.toList());
    }

    private List<String> filtrarEmailsValidos(List<UserEntity> clientes) {
        return clientes.stream()
                .filter(cliente -> cliente.getEmailNotifications() != null && cliente.getEmailNotifications())
                .filter(cliente -> cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty())
                .map(UserEntity::getEmail)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> filtrarPushTokensValidos(List<UserEntity> clientes) {
        return clientes.stream()
                .filter(cliente -> cliente.getPushToken() != null && !cliente.getPushToken().trim().isEmpty())
                .map(UserEntity::getPushToken)
                .distinct()
                .collect(Collectors.toList());
    }

    private Long calcularTotalDestinatarios(Notificacao notificacao) {
        try {
            List<UserEntity> destinatarios = obterDestinatarios(notificacao);
            return (long) destinatarios.size();
        } catch (Exception e) {
            logger.warn("Erro ao calcular total de destinatários para notificação {}: {}",
                    notificacao.getId(), e.getMessage());
            return 0L;
        }
    }

    private Long contarNotificacoesUrgentesDoDia(UUID organizadorId) {
        LocalDateTime inicioDoDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fimDoDia = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        return notificacaoRepository.findByOrganizadorId(organizadorId, Pageable.unpaged())
                .stream()
                .filter(n -> n.getTipo() == TipoNotificacao.URGENTE)
                .filter(n -> n.getCreatedAt().isAfter(inicioDoDia) && n.getCreatedAt().isBefore(fimDoDia))
                .count();
    }

    private NotificacaoResponse converterParaResponse(Notificacao notificacao) {
        NotificacaoResponse response = modelMapper.map(notificacao, NotificacaoResponse.class);

        // Adicionar dados da excursão se existir
        if (notificacao.getExcursao() != null) {
            response.setExcursaoId(notificacao.getExcursao().getId());
            response.setTituloExcursao(notificacao.getExcursao().getTitulo());
        }

        return response;
    }

//    // Métodos para uso interno e agendamento
//
//    @Async
//    public void enviarLembreteExcursao(UUID excursaoId) {
//        logger.info("Enviando lembrete automático para excursão: {}", excursaoId);
//
//        try {
//            // Buscar a excursão e seus inscritos
//            List<UserEntity> inscritos = clienteRepository.findByExcursaoId(excursaoId);
//
//            if (inscritos.isEmpty()) {
//                logger.info("Nenhum inscrito encontrado para a excursão: {}", excursaoId);
//                return;
//            }
//
//            // Criar mensagem de lembrete
//            String titulo = "Lembrete: Sua excursão é amanhã!";
//            String mensagem = "Não esqueça de sua excursão que acontece amanhã. Prepare-se e chegue no horário!";
//
//            // Enviar emails
//            List<String> emails = filtrarEmailsValidos(inscritos);
//            if (!emails.isEmpty()) {
//                // Aqui você criaria uma notificação temporária ou enviaria diretamente
//                logger.info("Enviando lembretes por email para {} clientes", emails.size());
//            }
//
//            // Enviar push notifications
//            List<String> pushTokens = filtrarPushTokensValidos(inscritos);
//            if (!pushTokens.isEmpty()) {
//                firebaseService.enviarNotificacaoMultipla(pushTokens, titulo, mensagem);
//                logger.info("Enviando lembretes push para {} dispositivos", pushTokens.size());
//            }
//
//        } catch (Exception e) {
//            logger.error("Erro ao enviar lembrete para excursão {}: {}", excursaoId, e.getMessage(), e);
//        }
//    }

    @Async
    public void enviarConfirmacaoInscricao(UUID inscricaoId, String nomeCliente, String tituloExcursao) {
        logger.info("Enviando confirmação de inscrição para: {}", nomeCliente);

        String titulo = "Inscrição Confirmada!";
        String mensagem = String.format("Sua inscrição na excursão '%s' foi confirmada com sucesso!", tituloExcursao);

        // Buscar cliente e enviar notificação
        // Implementação similar aos outros métodos de envio
    }

    @Async
    public void enviarNotificacaoPagamentoAprovado(UUID inscricaoId, String nomeCliente, String tituloExcursao) {
        logger.info("Enviando confirmação de pagamento para: {}", nomeCliente);

        String titulo = "Pagamento Aprovado!";
        String mensagem = String.format("Seu pagamento para a excursão '%s' foi aprovado. Prepare-se para a viagem!", tituloExcursao);

        // Implementação do envio
    }
}