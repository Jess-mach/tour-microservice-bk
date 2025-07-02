package br.com.tourapp.service;

import br.com.tourapp.dto.request.PagamentoCartaoRequest;
import br.com.tourapp.dto.request.PagamentoPixRequest;
import br.com.tourapp.dto.response.PagamentoResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface PaymentUseCase {
    PagamentoResponse criarPagamentoPix(@Valid PagamentoPixRequest request, UUID id);

    void processarWebhookMercadoPago(String paymentId);

    PagamentoResponse criarPagamentoCartao(@Valid PagamentoCartaoRequest request, UUID id);
}
