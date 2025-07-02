package br.com.tourapp.dto.response;


import br.com.tourapp.enums.TipoUsuario;

import java.util.UUID;

public class AuthResponse {

    private String token;
    private TipoUsuario tipoUsuario;
    private UUID userId;
    private String nome;
    private String email;

    // Construtores
    public AuthResponse() {}

    public AuthResponse(String token, TipoUsuario tipoUsuario, UUID userId, String nome, String email) {
        this.token = token;
        this.tipoUsuario = tipoUsuario;
        this.userId = userId;
        this.nome = nome;
        this.email = email;
    }

    // Getters e Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(TipoUsuario tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}