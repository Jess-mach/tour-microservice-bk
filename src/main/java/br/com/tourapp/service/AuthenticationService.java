package br.com.tourapp.service;

import br.com.tourapp.dto.request.CompleteProfileRequest;
import br.com.tourapp.dto.response.CompleteProfileResponse;
import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.TokenRefreshResponse;
import br.com.tourapp.entity.*;
import br.com.tourapp.enums.TipoUsuario;
import br.com.tourapp.exception.BusinessException;
import br.com.tourapp.repository.ClienteRepository;
import br.com.tourapp.repository.OrganizadorRepository;
import br.com.tourapp.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements AuthenticationUseCase{
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    private final ClienteRepository clienteRepository;

    private final OrganizadorRepository organizadorRepository;

    private final JwtUtils jwtUtils;

    /**
     * Método central para autenticação via Google
     */
    @Transactional
    public JwtResponse authenticateWithGoogle(String googleToken) {
        // Obter informações do usuário
        UserService.Pair<UserEntity, UserDetails> userInfo = userService.processGoogleToken(googleToken);

        // Gerar tokens JWT
        String accessToken = userService.generateAccessToken(userInfo.getSecond());

        // Gerar refresh token
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(
                userInfo.getFirst().getEmail(),
                userInfo.getSecond()
        );

        // Construir e retornar a resposta
        return userService.buildJwtResponse(
                userInfo.getFirst(),
                accessToken,
                refreshToken.getToken()
        );
    }

    /**
     * Método para renovar o token de acesso
     */
    public TokenRefreshResponse refreshToken(String refreshToken) {
        // Verificar e obter o refresh token
        RefreshTokenEntity tokenEntity = refreshTokenService.findAndValidateToken(refreshToken);

        // Obter detalhes do usuário
        SecurityUser securityUser = userService.loadSecurityUserByEmail(tokenEntity.getUserEmail());

        // Gerar novo token de acesso
        String newAccessToken = userService.generateAccessToken(securityUser);

        // Retornar resposta
        return new TokenRefreshResponse(
                newAccessToken,
                tokenEntity.getToken()
        );
    }

    @Transactional
    public CompleteProfileResponse completeProfile(CompleteProfileRequest request, String email, UUID userId) {

        // Verificar se é cliente
        Cliente cliente = clienteRepository.findByEmail(email).orElse(null);
        if (cliente != null) {
            return completeClienteProfile(cliente, request, userId);
        }

        // Verificar se é organizador
        Organizador organizador = organizadorRepository.findByEmail(email).orElse(null);
        if (organizador != null) {
            return completeOrganizadorProfile(organizador, request, userId);
        }

        throw new BusinessException("Usuário não encontrado");
    }

    private CompleteProfileResponse completeClienteProfile(Cliente cliente, CompleteProfileRequest request, UUID userId) {
        //cliente.setGoogleId(userId);

        // Atualizar dados do cliente
        if (request.getNome() != null) {
            cliente.setNome(request.getNome());
        }
        if (request.getTelefone() != null) {
            cliente.setTelefone(request.getTelefone());
        }
        if (request.getCep() != null) {
            cliente.setCep(request.getCep());
        }
        if (request.getEndereco() != null) {
            cliente.setEndereco(request.getEndereco());
        }
        if (request.getCidade() != null) {
            cliente.setCidade(request.getCidade());
        }
        if (request.getEstado() != null) {
            cliente.setEstado(request.getEstado());
        }

        cliente.setUpdatedAt(LocalDateTime.now());
        cliente = clienteRepository.save(cliente);

        CompleteProfileResponse response = new CompleteProfileResponse();
        response.setId(cliente.getId());
        response.setEmail(cliente.getEmail());
        response.setNome(cliente.getNome());
        response.setTelefone(cliente.getTelefone());
        response.setCep(cliente.getCep());
        response.setEndereco(cliente.getEndereco());
        response.setCidade(cliente.getCidade());
        response.setEstado(cliente.getEstado());
        response.setTipoUsuario(TipoUsuario.CLIENTE);
        response.setPerfilCompleto(isPerfilCompleto(cliente));

        return response;
    }

    private CompleteProfileResponse completeOrganizadorProfile(Organizador organizador, CompleteProfileRequest request, UUID userId) {
        //organizador.setGoogleId(userId);
        // Atualizar dados básicos
        if (request.getNome() != null) {
            organizador.setNome(request.getNome());
        }
        if (request.getTelefone() != null) {
            organizador.setTelefone(request.getTelefone());
        }
        if (request.getCep() != null) {
            organizador.setCep(request.getCep());
        }
        if (request.getEndereco() != null) {
            organizador.setEndereco(request.getEndereco());
        }
        if (request.getCidade() != null) {
            organizador.setCidade(request.getCidade());
        }
        if (request.getEstado() != null) {
            organizador.setEstado(request.getEstado());
        }

        // Atualizar dados específicos do organizador
        if (request.getCnpj() != null && !request.getCnpj().equals(organizador.getCnpj())) {
            if (organizadorRepository.existsByCnpj(request.getCnpj())) {
                throw new BusinessException("CNPJ já cadastrado");
            }
            organizador.setCnpj(request.getCnpj());
        }
        if (request.getNomeEmpresa() != null) {
            organizador.setNomeEmpresa(request.getNomeEmpresa());
        }
        if (request.getDescricao() != null) {
            organizador.setDescricao(request.getDescricao());
        }
        if (request.getSite() != null) {
            organizador.setSite(request.getSite());
        }

        organizador.setUpdatedAt(LocalDateTime.now());
        organizador = organizadorRepository.save(organizador);

        CompleteProfileResponse response = new CompleteProfileResponse();
        response.setId(organizador.getId());
        response.setEmail(organizador.getEmail());
        response.setNome(organizador.getNome());
        response.setTelefone(organizador.getTelefone());
        response.setCep(organizador.getCep());
        response.setEndereco(organizador.getEndereco());
        response.setCidade(organizador.getCidade());
        response.setEstado(organizador.getEstado());
        response.setCnpj(organizador.getCnpj());
        response.setNomeEmpresa(organizador.getNomeEmpresa());
        response.setDescricao(organizador.getDescricao());
        response.setSite(organizador.getSite());
        response.setTipoUsuario(TipoUsuario.ORGANIZADOR);
        response.setPerfilCompleto(isPerfilCompleto(organizador));

        return response;
    }

    private boolean isPerfilCompleto(Cliente cliente) {
        return cliente.getNome() != null &&
                cliente.getTelefone() != null &&
                cliente.getEndereco() != null &&
                cliente.getCidade() != null &&
                cliente.getEstado() != null;
    }

    private boolean isPerfilCompleto(Organizador organizador) {
        return organizador.getNome() != null &&
                organizador.getTelefone() != null &&
                organizador.getNomeEmpresa() != null &&
                organizador.getCnpj() != null &&
                organizador.getEndereco() != null &&
                organizador.getCidade() != null &&
                organizador.getEstado() != null;
    }
}