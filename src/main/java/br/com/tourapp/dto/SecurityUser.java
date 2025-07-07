package br.com.tourapp.dto;

import br.com.tourapp.entity.UserEntity;
import br.com.tourapp.enums.TipoUsuario;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SecurityUser implements UserDetails {
    private final UserDetails userDetails;
    private final UUID id;
    private final String email;
    private final String senha;
    private final String nome;
    private final TipoUsuario tipoUsuario;
    private final boolean ativo;

    public SecurityUser(UserEntity user, List<SimpleGrantedAuthority> authorities) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.senha = "";
        this.nome = user.getFullName();
        this.tipoUsuario = TipoUsuario.CLIENTE;
        this.ativo = user.isAtivo();

        // Criar UserDetails com as authorities corretas do banco
        this.userDetails = User.builder()
                .username(user.getEmail())
                .password("") // Não precisamos de senha com autenticação OAuth2
                .authorities(authorities)
                .accountExpired(!user.isAtivo())
                .accountLocked(!user.isAtivo())
                .credentialsExpired(false)
                .disabled(!user.isAtivo())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // CORREÇÃO: Usar authorities do userDetails quando disponível (que vem do banco)
        if (userDetails != null && userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
            return userDetails.getAuthorities();
        }

        // Fallback: usar tipoUsuario apenas quando userDetails não está disponível
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