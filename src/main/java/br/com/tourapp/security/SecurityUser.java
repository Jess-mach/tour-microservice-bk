package br.com.tourapp.security;

import br.com.tourapp.entity.Cliente;
import br.com.tourapp.entity.Organizador;
import br.com.tourapp.enums.TipoUsuario;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class SecurityUser implements UserDetails {

    private final UUID id;
    private final String email;
    private final String senha;
    private final String nome;
    private final TipoUsuario tipoUsuario;
    private final boolean ativo;

    public SecurityUser(Cliente cliente) {
        this.id = cliente.getId();
        this.email = cliente.getEmail();
        this.senha = cliente.getSenha();
        this.nome = cliente.getNome();
        this.tipoUsuario = cliente.getTipoUsuario();
        this.ativo = cliente.getAtivo();
    }

    public SecurityUser(Organizador organizador) {
        this.id = organizador.getId();
        this.email = organizador.getEmail();
        this.senha = organizador.getSenha();
        this.nome = organizador.getNomeEmpresa();
        this.tipoUsuario = organizador.getTipoUsuario();
        this.ativo = organizador.getStatus().name().equals("ATIVO");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "ROLE_" + tipoUsuario.name();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    // Getters adicionais
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }
}

