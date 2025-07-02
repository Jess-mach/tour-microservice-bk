package br.com.tourapp.tourapp.repository;

import br.com.tourapp.tourapp.entity.Organizador;
import br.com.tourapp.tourapp.enums.StatusOrganizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizadorRepository extends JpaRepository<Organizador, UUID> {

    Optional<Organizador> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Organizador> findByStatus(StatusOrganizador status);

    @Query("SELECT o FROM Organizador o WHERE o.status = 'ATIVO'")
    List<Organizador> findAtivos();

    @Query("SELECT COUNT(o) FROM Organizador o WHERE o.status = :status")
    Long countByStatus(@Param("status") StatusOrganizador status);

    boolean existsByCnpj(String cnpj);
}

