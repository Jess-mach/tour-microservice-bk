package br.com.tourapp.tourapp.security;

import br.com.tourapp.entity.Cliente;
import br.com.tourapp.entity.Organizador;
import br.com.tourapp.repository.ClienteRepository;
import br.com.tourapp.repository.OrganizadorRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;
    private final OrganizadorRepository organizadorRepository;

    public CustomUserDetailsService(ClienteRepository clienteRepository,
                                  OrganizadorRepository organizadorRepository) {
        this.clienteRepository = clienteRepository;
        this.organizadorRepository = organizadorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tentar encontrar como cliente primeiro
        Optional<Cliente> cliente = clienteRepository.findByEmail(email);
        if (cliente.isPresent()) {
            return new SecurityUser(cliente.get());
        }

        // Se não encontrou como cliente, tentar como organizador
        Optional<Organizador> organizador = organizadorRepository.findByEmail(email);
        if (organizador.isPresent()) {
            return new SecurityUser(organizador.get());
        }

        throw new UsernameNotFoundException("Usuário não encontrado com email: " + email);
    }
}

