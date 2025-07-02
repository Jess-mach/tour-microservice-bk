package br.com.tourapp.tourapp.service;

import br.com.tourapp.tourapp.dto.response.ClienteResponse;
import br.com.tourapp.tourapp.entity.Cliente;
import br.com.tourapp.tourapp.exception.NotFoundException;
import br.com.tourapp.tourapp.repository.ClienteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ModelMapper modelMapper;

    public ClienteService(ClienteRepository clienteRepository, ModelMapper modelMapper) {
        this.clienteRepository = clienteRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    public ClienteResponse obterPerfil(UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        return modelMapper.map(cliente, ClienteResponse.class);
    }

    public ClienteResponse atualizarPerfil(UUID clienteId, ClienteResponse request) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        cliente.setNome(request.getNome());
        cliente.setTelefone(request.getTelefone());
        cliente.setEmailNotifications(request.getEmailNotifications());
        cliente.setSmsNotifications(request.getSmsNotifications());

        cliente = clienteRepository.save(cliente);
        return modelMapper.map(cliente, ClienteResponse.class);
    }

    public void atualizarPushToken(UUID clienteId, String pushToken) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        cliente.setPushToken(pushToken);
        clienteRepository.save(cliente);
    }

    public void atualizarConfiguracoes(UUID clienteId, Boolean emailNotifications, Boolean smsNotifications) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        cliente.setEmailNotifications(emailNotifications);
        cliente.setSmsNotifications(smsNotifications);
        clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente obterPorId(UUID clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
    }
}

