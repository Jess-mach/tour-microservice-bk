
// ============================================
// CompaniaFiltroRequest.java
// ============================================
package br.com.tourapp.dto.request;

import br.com.tourapp.dto.enums.RoleCompania;
import br.com.tourapp.dto.enums.StatusCompania;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CompaniaFiltroRequest {

    private String termo; // Busca por nome da empresa
    private StatusCompania status;
    private String cidade;
    private String estado;
    private String cnpj;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicioMinima;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicioMaxima;

    private Boolean apenasComExcursoes;
    private Boolean apenasMinhasCompanias;

    // Ordenação
    private String ordenarPor = "nomeEmpresa"; // nomeEmpresa, dataCreatedAt, totalExcursoes
    private String direcao = "ASC"; // ASC, DESC
}
