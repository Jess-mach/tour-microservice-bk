package br.com.tourapp.tourapp.controller;

import br.com.tourapp.dto.request.CadastroClienteRequest;
import br.com.tourapp.dto.request.CadastroOrganizadorRequest;
import br.com.tourapp.dto.request.LoginRequest;
import br.com.tourapp.dto.response.AuthResponse;
import br.com.tourapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cadastro/cliente")
    public ResponseEntity<AuthResponse> cadastroCliente(@Valid @RequestBody CadastroClienteRequest request) {
        AuthResponse response = authService.cadastrarCliente(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cadastro/organizador")
    public ResponseEntity<AuthResponse> cadastroOrganizador(@Valid @RequestBody CadastroOrganizadorRequest request) {
        AuthResponse response = authService.cadastrarOrganizador(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String token) {
        AuthResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(response);
    }
}

