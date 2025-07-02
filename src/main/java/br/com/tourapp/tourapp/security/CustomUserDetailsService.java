package br.com.tourapp.security;

import br.com.tourapp.entity.Cliente;
import br.com.tourapp.entity.Organizador;
import br.com.tourapp.repository.ClienteRepository;
import br.com.tourapp.repository.OrganizadorRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
        // Primeiro tenta encontrar cliente
        Cliente cliente = clienteRepository.findByEmail(email).orElse(null);
        if (cliente != null) {
            return new SecurityUser(cliente);
        }

        // Se não encontrou cliente, tenta organizador
        Organizador organizador = organizadorRepository.findByEmail(email).orElse(null);
        if (organizador != null) {
            return new SecurityUser(organizador);
        }

        throw new UsernameNotFoundException("Usuário não encontrado com email: " + email);
    }
}

