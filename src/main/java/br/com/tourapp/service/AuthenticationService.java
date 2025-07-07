package br.com.tourapp.service;

import br.com.tourapp.dto.SecurityUser;
import br.com.tourapp.dto.enums.RoleCompania;
import br.com.tourapp.dto.request.CompleteProfileRequest;
import br.com.tourapp.dto.response.CompleteProfileResponse;
import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.TokenRefreshResponse;
import br.com.tourapp.entity.*;
import br.com.tourapp.enums.TipoUsuario;
import br.com.tourapp.exception.BusinessException;
import br.com.tourapp.repository.UserRepository;
import br.com.tourapp.repository.CompaniaRepository;
import br.com.tourapp.repository.UserCompaniaRepository;
import br.com.tourapp.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements AuthenticationUseCase {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final CompaniaRepository companiaRepository;
    private final UserCompaniaRepository userCompaniaRepository;
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
        // Buscar usuário unificado
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        // Atualizar dados básicos do usuário
        if (request.getNome() != null) {
            user.setFullName(request.getNome());
        }
        if (request.getTelefone() != null) {
            user.setPhone(request.getTelefone());
        }
        if (request.getCep() != null) {
            user.setCep(request.getCep());
        }
        if (request.getEndereco() != null) {
            user.setEndereco(request.getEndereco());
        }
        if (request.getCidade() != null) {
            user.setCidade(request.getCidade());
        }
        if (request.getEstado() != null) {
            user.setEstado(request.getEstado());
        }

        // Se tem dados de empresa, é organizador
        if (temDadosEmpresa(request)) {
            return completeOrganizadorProfile(user, request);
        } else {
            return completeClienteProfile(user, request);
        }
    }

    private CompleteProfileResponse completeClienteProfile(UserEntity user, CompleteProfileRequest request) {
        // Apenas salvar o usuário (dados básicos já foram atualizados)
        user = userRepository.save(user);

        CompleteProfileResponse response = new CompleteProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setNome(user.getFullName());
        response.setTelefone(user.getPhone());
        response.setCep(user.getCep());
        response.setEndereco(user.getEndereco());
        response.setCidade(user.getCidade());
        response.setEstado(user.getEstado());
        response.setTipoUsuario(TipoUsuario.CLIENTE);
        response.setPerfilCompleto(isPerfilCompletoCliente(user));

        return response;
    }

    private CompleteProfileResponse completeOrganizadorProfile(UserEntity user, CompleteProfileRequest request) {
        // Verificar se já tem compania
        var companias = companiaRepository.findByUserId(user.getId());
        CompaniaEntity compania;

        if (companias.isEmpty()) {
            // Criar nova compania
            compania = new CompaniaEntity();
            compania.setNomeEmpresa(request.getNomeEmpresa());
            compania.setCnpj(request.getCnpj());
            compania.setDescricao(request.getDescricao());
            compania.setSite(request.getSite());

            // Copiar endereço do usuário para a empresa se não fornecido
            if (compania.getCep() == null) compania.setCep(user.getCep());
            if (compania.getEndereco() == null) compania.setEndereco(user.getEndereco());
            if (compania.getCidade() == null) compania.setCidade(user.getCidade());
            if (compania.getEstado() == null) compania.setEstado(user.getEstado());

            // Validar CNPJ único
            if (request.getCnpj() != null && companiaRepository.existsByCnpj(request.getCnpj())) {
                throw new BusinessException("CNPJ já cadastrado");
            }

            compania = companiaRepository.save(compania);

            // Criar relacionamento user-compania como ADMIN
            UserCompaniaEntity userCompania = new UserCompaniaEntity(user, compania, RoleCompania.ADMIN);
            userCompaniaRepository.save(userCompania);
        } else {
            // Atualizar compania existente
            compania = companias.get(0);
            if (request.getNomeEmpresa() != null) {
                compania.setNomeEmpresa(request.getNomeEmpresa());
            }
            if (request.getCnpj() != null && !request.getCnpj().equals(compania.getCnpj())) {
                if (companiaRepository.existsByCnpj(request.getCnpj())) {
                    throw new BusinessException("CNPJ já cadastrado");
                }
                compania.setCnpj(request.getCnpj());
            }
            if (request.getDescricao() != null) {
                compania.setDescricao(request.getDescricao());
            }
            if (request.getSite() != null) {
                compania.setSite(request.getSite());
            }
            compania = companiaRepository.save(compania);
        }

        // Salvar usuário
        user = userRepository.save(user);

        CompleteProfileResponse response = new CompleteProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setNome(user.getFullName());
        response.setTelefone(user.getPhone());
        response.setCep(user.getCep());
        response.setEndereco(user.getEndereco());
        response.setCidade(user.getCidade());
        response.setEstado(user.getEstado());
        response.setCnpj(compania.getCnpj());
        response.setNomeEmpresa(compania.getNomeEmpresa());
        response.setDescricao(compania.getDescricao());
        response.setSite(compania.getSite());
        response.setTipoUsuario(TipoUsuario.ORGANIZADOR);
        response.setPerfilCompleto(isPerfilCompletoOrganizador(user, compania));

        return response;
    }

    private boolean temDadosEmpresa(CompleteProfileRequest request) {
        return request.getNomeEmpresa() != null ||
                request.getCnpj() != null ||
                request.getDescricao() != null ||
                request.getSite() != null;
    }

    private boolean isPerfilCompletoCliente(UserEntity user) {
        return user.getFullName() != null && !user.getFullName().trim().isEmpty() &&
                user.getPhone() != null && !user.getPhone().trim().isEmpty() &&
                user.getEndereco() != null && !user.getEndereco().trim().isEmpty() &&
                user.getCidade() != null && !user.getCidade().trim().isEmpty() &&
                user.getEstado() != null && !user.getEstado().trim().isEmpty();
    }

    private boolean isPerfilCompletoOrganizador(UserEntity user, CompaniaEntity compania) {
        return isPerfilCompletoCliente(user) &&
                compania.getNomeEmpresa() != null && !compania.getNomeEmpresa().trim().isEmpty() &&
                compania.getCnpj() != null && !compania.getCnpj().trim().isEmpty();
    }
}