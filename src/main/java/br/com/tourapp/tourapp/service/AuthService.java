package br.com.tourapp.service;

import br.com.tourapp.dto.request.CadastroClienteRequest;
import br.com.tourapp.dto.request.CadastroOrganizadorRequest;
import br.com.tourapp.dto.request.LoginRequest;
import br.com.tourapp.dto.response.AuthResponse;
import br.com.tourapp.entity.Cliente;
import br.com.tourapp.entity.Organizador;
import br.com.tourapp.enums.StatusOrganizador;
import br.com.tourapp.enums.TipoUsuario;
import br.com.tourapp.exception.BusinessException;
import br.com.tourapp.exception.UnauthorizedException;
import br.com.tourapp.repository.ClienteRepository;
import br.com.tourapp.repository.OrganizadorRepository;
import br.com.tourapp.tourapp.security.JwtUtil;
import br.com.tourapp.tourapp.security.SecurityUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final ClienteRepository clienteRepository;
    private final OrganizadorRepository organizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(ClienteRepository clienteRepository,
                      OrganizadorRepository organizadorRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      AuthenticationManager authenticationManager) {
        this.clienteRepository = clienteRepository;
        this.organizadorRepository = organizadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );

            SecurityUser user = (SecurityUser) authentication.getPrincipal();
            String token = jwtUtil.generateToken(user, user.getId(), user.getTipoUsuario());

            return new AuthResponse(
                token,
                user.getTipoUsuario(),
                user.getId(),
                user.getNome(),
                user.getEmail()
            );

        } catch (Exception e) {
            throw new UnauthorizedException("Email ou senha inválidos");
        }
    }

    public AuthResponse cadastrarCliente(CadastroClienteRequest request) {
        // Verificar se o email já existe
        if (clienteRepository.existsByEmail(request.getEmail()) || 
            organizadorRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já está em uso");
        }

        // Criar novo cliente
        Cliente cliente = new Cliente();
        cliente.setNome(request.getNome());
        cliente.setEmail(request.getEmail());
        cliente.setSenha(passwordEncoder.encode(request.getSenha()));
        cliente.setTelefone(request.getTelefone());

        cliente = clienteRepository.save(cliente);

        // Gerar token
        SecurityUser user = new SecurityUser(cliente);
        String token = jwtUtil.generateToken(user, cliente.getId(), TipoUsuario.CLIENTE);

        return new AuthResponse(
            token,
            TipoUsuario.CLIENTE,
            cliente.getId(),
            cliente.getNome(),
            cliente.getEmail()
        );
    }

    public AuthResponse cadastrarOrganizador(CadastroOrganizadorRequest request) {
        // Verificar se o email já existe
        if (clienteRepository.existsByEmail(request.getEmail()) || 
            organizadorRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já está em uso");
        }

        // Criar novo organizador
        Organizador organizador = new Organizador();
        organizador.setNomeEmpresa(request.getNomeEmpresa());
        organizador.setNomeResponsavel(request.getNomeResponsavel());
        organizador.setEmail(request.getEmail());
        organizador.setSenha(passwordEncoder.encode(request.getSenha()));
        organizador.setTelefone(request.getTelefone());
        organizador.setPixKey(request.getPixKey());
        organizador.setCnpj(request.getCnpj());
        organizador.setStatus(StatusOrganizador.ATIVO); // Para MVP, aprovar automaticamente

        organizador = organizadorRepository.save(organizador);

        // Gerar token
        SecurityUser user = new SecurityUser(organizador);
        String token = jwtUtil.generateToken(user, organizador.getId(), TipoUsuario.ORGANIZADOR);

        return new AuthResponse(
            token,
            TipoUsuario.ORGANIZADOR,
            organizador.getId(),
            organizador.getNomeEmpresa(),
            organizador.getEmail()
        );
    }

    public AuthResponse refreshToken(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Token inválido");
        }

        String email = jwtUtil.extractUsername(token);
        TipoUsuario tipoUsuario = jwtUtil.extractTipoUsuario(token);

        SecurityUser user;
        String nome;

        if (tipoUsuario == TipoUsuario.CLIENTE) {
            Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
            user = new SecurityUser(cliente);
            nome = cliente.getNome();
        } else {
            Organizador organizador = organizadorRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
            user = new SecurityUser(organizador);
            nome = organizador.getNomeEmpresa();
        }

        String newToken = jwtUtil.generateToken(user, user.getId(), user.getTipoUsuario());

        return new AuthResponse(
            newToken,
            user.getTipoUsuario(),
            user.getId(),
            nome,
            user.getEmail()
        );
    }
}

