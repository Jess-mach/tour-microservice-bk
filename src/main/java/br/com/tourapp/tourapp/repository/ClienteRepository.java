package br.com.tourapp.tourapp.repository;

import br.com.tourapp.tourapp.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Cliente> findByAtivoTrue();

    @Query("SELECT c FROM Cliente c WHERE c.emailNotifications = true")
    List<Cliente> findByEmailNotificationsTrue();

    @Query("SELECT c FROM Cliente c WHERE c.pushToken IS NOT NULL")
    List<Cliente> findByPushTokenNotNull();

    @Query("SELECT c FROM Cliente c JOIN c.inscricoes i WHERE i.excursao.id = :excursaoId")
    List<Cliente> findByExcursaoId(@Param("excursaoId") UUID excursaoId);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.ativo = true")
    Long countAtivos();
}