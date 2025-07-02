package br.com.tourapp.controller;

import br.com.tourapp.dto.request.PagamentoCartaoRequest;
import br.com.tourapp.dto.request.PagamentoPixRequest;
import br.com.tourapp.dto.response.PagamentoResponse;
import br.com.tourapp.service.PagamentoService;
import br.com.tourapp.security.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagamentos")
@PreAuthorize("hasRole('CLIENTE')")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/pix")
    public ResponseEntity<PagamentoResponse> criarPagamentoPix(
            @Valid @RequestBody PagamentoPixRequest request,
            @AuthenticationPrincipal SecurityUser user) {
        PagamentoResponse response = pagamentoService.criarPagamentoPix(request, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cartao")
    public ResponseEntity<PagamentoResponse> criarPagamentoCartao(
            @Valid @RequestBody PagamentoCartaoRequest request,
            @AuthenticationPrincipal SecurityUser user) {
        PagamentoResponse response = pagamentoService.criarPagamentoCartao(request, user.getId());
        return ResponseEntity.ok(response);
    }
}

