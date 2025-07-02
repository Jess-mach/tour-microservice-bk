package br.com.tourapp.tourapp.controller;

import br.com.tourapp.dto.request.InscricaoRequest;
import br.com.tourapp.dto.response.ExcursaoResponse;
import br.com.tourapp.dto.response.InscricaoResponse;
import br.com.tourapp.service.ExcursaoService;
import br.com.tourapp.service.InscricaoService;
import br.com.tourapp.tourapp.security.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/public")
@CrossOrigin(origins = "*")
public class PublicController {

    private final ExcursaoService excursaoService;
    private final InscricaoService inscricaoService;

    public PublicController(ExcursaoService excursaoService, InscricaoService inscricaoService) {
        this.excursaoService = excursaoService;
        this.inscricaoService = inscricaoService;
    }

    @GetMapping("/excursoes/{id}")
    public ResponseEntity<ExcursaoResponse> obterExcursaoPublica(@PathVariable UUID id) {
        ExcursaoResponse response = excursaoService.obterExcursaoPublica(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/excursoes/{excursaoId}/inscricoes")
    public ResponseEntity<InscricaoResponse> inscreverNaExcursao(
            @PathVariable UUID excursaoId,
            @Valid @RequestBody InscricaoRequest request,
            @AuthenticationPrincipal SecurityUser user) {
        InscricaoResponse response = inscricaoService.criarInscricao(excursaoId, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}

