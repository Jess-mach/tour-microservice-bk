//src/main/java/br/com/tourapp/service/PagamentoService.java
package br.com.tourapp.service;

import br.com.tourapp.dto.request.PagamentoCartaoRequest;
import br.com.tourapp.dto.request.PagamentoPixRequest;
import br.com.tourapp.dto.response.PagamentoResponse;
import br.com.tourapp.entity.Inscricao;
import br.com.tourapp.entity.Pagamento;
import br.com.tourapp.enums.MetodoPagamento;
import br.com.tourapp.enums.StatusPagamento;
import br.com.tourapp.exception.BusinessException;
import br.com.tourapp.exception.NotFoundException;
import br.com.tourapp.repository.PagamentoRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PagamentoService implements PaymentUseCase {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);

    private final PagamentoRepository pagamentoRepository;
    private final InscricaoService inscricaoService;
    private final ModelMapper modelMapper;
    private final PaymentClient paymentClient;

    @Value("${app.mercadopago.sandbox:true}")
    private boolean sandbox;

    public PagamentoService(PagamentoRepository pagamentoRepository,
                            InscricaoService inscricaoService,
                            ModelMapper modelMapper,
                            @Value("${app.mercadopago.access-token}") String accessToken) {
        this.pagamentoRepository = pagamentoRepository;
        this.inscricaoService = inscricaoService;
        this.modelMapper = modelMapper;

        // Configurar Mercado Pago
        MercadoPagoConfig.setAccessToken(accessToken);
        this.paymentClient = new PaymentClient();

        logger.info("PagamentoService inicializado. Sandbox: {}", sandbox);
    }

    public PagamentoResponse criarPagamentoPix(PagamentoPixRequest request, UUID clienteId) {
        logger.info("Criando pagamento PIX para inscricao: {} do cliente: {}", request.getInscricaoId(), clienteId);

        Inscricao inscricao = inscricaoService.obterPorId(request.getInscricaoId());

        // Validações de negócio
        validarInscricaoParaPagamento(inscricao, clienteId);

        if (!inscricao.getExcursao().getAceitaPix()) {
            throw new BusinessException("Esta excursão não aceita pagamento via PIX");
        }

        try {
            // Criar pagamento no Mercado Pago
            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                    .transactionAmount(inscricao.getValorPago())
                    .description("Excursão: " + inscricao.getExcursao().getTitulo())
                    .paymentMethodId("pix")
                    .payer(PaymentPayerRequest.builder()
                            .email(inscricao.getCliente().getEmail())
                            .firstName(inscricao.getCliente().getNome())
                            .identification(IdentificationRequest.builder()
                                    .type("CPF") // Pode ser CPF ou CNPJ
                                    .number("11111111111") // Em produção, capturar do cliente
                                    .build())
                            .build())
                    .externalReference(inscricao.getId().toString())
                    .notificationUrl(construirUrlWebhook())
                    .build();

            Payment payment = paymentClient.create(paymentCreateRequest);

            logger.info("Pagamento PIX criado no MP. ID: {}", payment.getId());

            // Salvar pagamento local
            Pagamento pagamento = new Pagamento();
            pagamento.setInscricao(inscricao);
            pagamento.setValor(inscricao.getValorPago());
            pagamento.setMetodoPagamento(MetodoPagamento.PIX);
            pagamento.setStatus(StatusPagamento.PENDENTE);
            pagamento.setMercadoPagoPaymentId(payment.getId().toString());

            // Dados do PIX
            if (payment.getPointOfInteraction() != null &&
                    payment.getPointOfInteraction().getTransactionData() != null) {
                pagamento.setQrCode(payment.getPointOfInteraction().getTransactionData().getQrCode());
                pagamento.setQrCodeBase64(payment.getPointOfInteraction().getTransactionData().getQrCodeBase64());
            }

            pagamento.setDataVencimento(LocalDateTime.now().plusMinutes(30)); // PIX expira em 30 min
            pagamento.setObservacoes("Pagamento PIX gerado automaticamente");

            pagamento = pagamentoRepository.save(pagamento);

            logger.info("Pagamento PIX salvo localmente. ID: {}", pagamento.getId());

            return converterParaResponse(pagamento);

        } catch (MPException | MPApiException e) {
            logger.error("Erro ao criar pagamento PIX no Mercado Pago: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao processar pagamento PIX: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar pagamento PIX: {}", e.getMessage(), e);
            throw new BusinessException("Erro interno ao processar pagamento");
        }
    }

    public PagamentoResponse criarPagamentoCartao(PagamentoCartaoRequest request, UUID clienteId) {
        logger.info("Criando pagamento cartão para inscricao: {} do cliente: {}", request.getInscricaoId(), clienteId);

        Inscricao inscricao = inscricaoService.obterPorId(request.getInscricaoId());

        // Validações de negócio
        validarInscricaoParaPagamento(inscricao, clienteId);

        if (!inscricao.getExcursao().getAceitaCartao()) {
            throw new BusinessException("Esta excursão não aceita pagamento via cartão");
        }

        // Validar dados do cartão
        validarDadosCartao(request);

        try {
            // Criar pagamento no Mercado Pago
            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                    .transactionAmount(inscricao.getValorPago())
                    .description("Excursão: " + inscricao.getExcursao().getTitulo())
                    .installments(request.getParcelas())
                    .paymentMethodId(detectarBandeiraCartao(request.getNumeroCartao()))
                    .payer(PaymentPayerRequest.builder()
                            .email(inscricao.getCliente().getEmail())
                            .firstName(inscricao.getCliente().getNome())
                            .identification(IdentificationRequest.builder()
                                    .type("CPF")
                                    .number("11111111111") // Em produção, capturar do cliente
                                    .build())
                            .build())
                    .token(gerarTokenCartao(request)) // Em produção, usar SDK do MP no frontend
                    .externalReference(inscricao.getId().toString())
                    .notificationUrl(construirUrlWebhook())
                    .build();

            Payment payment = paymentClient.create(paymentCreateRequest);

            logger.info("Pagamento cartão criado no MP. ID: {}, Status: {}", payment.getId(), payment.getStatus());

            // Salvar pagamento local
            Pagamento pagamento = new Pagamento();
            pagamento.setInscricao(inscricao);
            pagamento.setValor(inscricao.getValorPago());
            pagamento.setMetodoPagamento(MetodoPagamento.CARTAO_CREDITO);
            pagamento.setStatus(mapearStatusMercadoPago(payment.getStatus()));
            pagamento.setMercadoPagoPaymentId(payment.getId().toString());
            pagamento.setDataProcessamento(LocalDateTime.now());
            pagamento.setObservacoes("Pagamento cartão - " + request.getParcelas() + "x");

            // Se foi aprovado instantaneamente, processar
            if (payment.getStatus().equals("approved")) {
                pagamento.setDataProcessamento(LocalDateTime.now());
            }

            pagamento = pagamentoRepository.save(pagamento);

            // Atualizar status da inscrição se aprovado
            if (pagamento.getStatus() == StatusPagamento.APROVADO) {
                inscricaoService.atualizarStatusPagamento(inscricao.getId(), StatusPagamento.APROVADO);
                logger.info("Pagamento cartão aprovado instantaneamente. Inscricao: {}", inscricao.getId());
            }

            return converterParaResponse(pagamento);

        } catch (MPException | MPApiException e) {
            logger.error("Erro ao criar pagamento cartão no Mercado Pago: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao processar pagamento: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar pagamento cartão: {}", e.getMessage(), e);
            throw new BusinessException("Erro interno ao processar pagamento");
        }
    }

    public void processarWebhookMercadoPago(String paymentId) {
        logger.info("Processando webhook do Mercado Pago para pagamento: {}", paymentId);

        try {
            Payment payment = paymentClient.get(Long.parseLong(paymentId));

            Pagamento pagamento = pagamentoRepository.findByMercadoPagoPaymentId(paymentId)
                    .orElseThrow(() -> new NotFoundException("Pagamento não encontrado com ID MP: " + paymentId));

            StatusPagamento statusAnterior = pagamento.getStatus();
            StatusPagamento novoStatus = mapearStatusMercadoPago(payment.getStatus());

            if (!statusAnterior.equals(novoStatus)) {
                pagamento.setStatus(novoStatus);
                pagamento.setDataProcessamento(LocalDateTime.now());

                // Adicionar observações sobre a mudança de status
                String observacao = String.format("Status alterado de %s para %s via webhook",
                        statusAnterior, novoStatus);
                pagamento.setObservacoes(pagamento.getObservacoes() + " | " + observacao);

                pagamentoRepository.save(pagamento);

                logger.info("Status do pagamento {} alterado de {} para {}",
                        pagamento.getId(), statusAnterior, novoStatus);

                // Atualizar inscrição se foi aprovado
                if (novoStatus == StatusPagamento.APROVADO && statusAnterior != StatusPagamento.APROVADO) {
                    inscricaoService.atualizarStatusPagamento(pagamento.getInscricao().getId(), StatusPagamento.APROVADO);
                    logger.info("Inscrição {} aprovada via webhook", pagamento.getInscricao().getId());
                }

                // Se foi cancelado/rejeitado, liberar vaga
                if ((novoStatus == StatusPagamento.CANCELADO || novoStatus == StatusPagamento.REJEITADO) &&
                        statusAnterior == StatusPagamento.APROVADO) {
                    inscricaoService.atualizarStatusPagamento(pagamento.getInscricao().getId(), novoStatus);
                    logger.info("Inscrição {} cancelada/rejeitada via webhook", pagamento.getInscricao().getId());
                }
            }

        } catch (NumberFormatException e) {
            logger.error("ID de pagamento inválido: {}", paymentId);
        } catch (NotFoundException e) {
            logger.warn("Pagamento não encontrado na base local: {}", paymentId);
        } catch (Exception e) {
            logger.error("Erro ao processar webhook para pagamento {}: {}", paymentId, e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Pagamento obterPorId(UUID pagamentoId) {
        return pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado"));
    }

    // Métodos auxiliares privados

    private void validarInscricaoParaPagamento(Inscricao inscricao, UUID clienteId) {
        if (!inscricao.getCliente().getId().equals(clienteId)) {
            throw new BusinessException("Inscrição não pertence ao cliente");
        }

        if (inscricao.getStatusPagamento() == StatusPagamento.APROVADO) {
            throw new BusinessException("Pagamento já foi aprovado para esta inscrição");
        }

        if (!inscricao.getExcursao().isAtiva()) {
            throw new BusinessException("Não é possível pagar por uma excursão inativa");
        }

        if (inscricao.getExcursao().getDataSaida().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível pagar por uma excursão que já aconteceu");
        }
    }

    private void validarDadosCartao(PagamentoCartaoRequest request) {
        if (request.getParcelas() < 1 || request.getParcelas() > 12) {
            throw new BusinessException("Número de parcelas deve ser entre 1 e 12");
        }

        // Validação básica do número do cartão (Luhn algorithm seria ideal)
        if (request.getNumeroCartao().length() < 13 || request.getNumeroCartao().length() > 19) {
            throw new BusinessException("Número do cartão inválido");
        }

        // Validar CVV
        if (request.getCvv().length() < 3 || request.getCvv().length() > 4) {
            throw new BusinessException("CVV inválido");
        }

        // Validar data de expiração
        try {
            int mes = Integer.parseInt(request.getMesExpiracao());
            int ano = Integer.parseInt(request.getAnoExpiracao());

            if (mes < 1 || mes > 12) {
                throw new BusinessException("Mês de expiração inválido");
            }

            LocalDateTime agora = LocalDateTime.now();
            if (ano < agora.getYear() || (ano == agora.getYear() && mes < agora.getMonthValue())) {
                throw new BusinessException("Cartão expirado");
            }
        } catch (NumberFormatException e) {
            throw new BusinessException("Data de expiração inválida");
        }
    }

    private String detectarBandeiraCartao(String numeroCartao) {
        // Detectar bandeira baseado nos primeiros dígitos
        String numero = numeroCartao.replaceAll("\\s", "");

        if (numero.startsWith("4")) {
            return "visa";
        } else if (numero.startsWith("5") || numero.startsWith("2")) {
            return "master";
        } else if (numero.startsWith("3")) {
            return "amex";
        } else if (numero.startsWith("6")) {
            return "elo";
        } else {
            return "visa"; // Default
        }
    }

    private String gerarTokenCartao(PagamentoCartaoRequest request) {
        // Em produção, o token seria gerado no frontend usando o SDK do Mercado Pago
        // Para o MVP, retornamos um token fictício
        // IMPORTANTE: Implementar tokenização real antes da produção por segurança
        return "CARD_TOKEN_" + System.currentTimeMillis();
    }

    private String construirUrlWebhook() {
        // Em produção, usar variável de ambiente com a URL real
        return "https://seu-app.railway.app/api/webhook/mercadopago";
    }

    private StatusPagamento mapearStatusMercadoPago(String status) {
        return switch (status.toLowerCase()) {
            case "approved" -> StatusPagamento.APROVADO;
            case "pending" -> StatusPagamento.PENDENTE;
            case "in_process" -> StatusPagamento.PROCESSANDO;
            case "rejected" -> StatusPagamento.REJEITADO;
            case "cancelled" -> StatusPagamento.CANCELADO;
            case "refunded" -> StatusPagamento.REEMBOLSADO;
            case "charged_back" -> StatusPagamento.CANCELADO;
            default -> {
                logger.warn("Status desconhecido do Mercado Pago: {}", status);
                yield StatusPagamento.PENDENTE;
            }
        };
    }

    private PagamentoResponse converterParaResponse(Pagamento pagamento) {
        PagamentoResponse response = modelMapper.map(pagamento, PagamentoResponse.class);

        // Adicionar link de pagamento se necessário
        if (pagamento.getMetodoPagamento() == MetodoPagamento.CARTAO_CREDITO
                && pagamento.getMercadoPagoPaymentId() != null) {

            if (sandbox) {
                response.setLinkPagamento("https://sandbox.mercadopago.com.br/checkout/v1/redirect?pref_id="
                        + pagamento.getMercadoPagoPaymentId());
            } else {
                response.setLinkPagamento("https://www.mercadopago.com.br/checkout/v1/redirect?pref_id="
                        + pagamento.getMercadoPagoPaymentId());
            }
        }

        return response;
    }
}