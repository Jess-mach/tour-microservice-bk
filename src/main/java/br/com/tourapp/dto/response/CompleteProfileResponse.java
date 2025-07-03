package br.com.tourapp.dto.response;

import br.com.tourapp.enums.TipoUsuario;
import lombok.Data;

import java.util.UUID;

@Data
public class CompleteProfileResponse {
    private UUID id;
    private String email;
    private String nome;
    private String telefone;
    private String cep;
    private String endereco;
    private String cidade;
    private String estado;
    private String googleId;
    private TipoUsuario tipoUsuario;
    private Boolean perfilCompleto;

    // Campos específicos do organizador
    private String cnpj;
    private String nomeEmpresa;
    private String descricao;
    private String site;
}