package br.com.tourapp.controller;

import br.com.tourapp.service.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@CrossOrigin(origins = "*")
public class WebhookController {

    private final PagamentoService pagamentoService;

    public WebhookController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/mercadopago")
    public ResponseEntity<String> webhookMercadoPago(@RequestBody Map<String, Object> payload) {
        try {
            // Verificar se é um evento de pagamento
            if ("payment".equals(payload.get("type"))) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                String paymentId = (String) data.get("id");

                pagamentoService.processarWebhookMercadoPago(paymentId);
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.err.println("Erro ao processar webhook: " + e.getMessage());
            return ResponseEntity.ok("OK"); // Sempre retornar OK para o MP
        }
    }
}