package br.com.tourapp.dto.request;

import br.com.tourapp.enums.TipoUsuario;

import java.util.UUID;


public record UpdateUserRequest(
        UUID id,
        String email,
        String senha,
        String nome,
        String telefone,
        TipoUsuario tipoUsuario,
        boolean ativo,
        String cep,
        String endereco,
        String cidade,
        String estado
)  {





}