package br.com.tourapp.repository;

import br.com.tourapp.entity.Excursao;
import br.com.tourapp.enums.StatusExcursao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExcursaoRepository extends JpaRepository<Excursao, UUID> {

    Page<Excursao> findByOrganizadorId(UUID organizadorId, Pageable pageable);

    Page<Excursao> findByOrganizadorIdAndStatus(UUID organizadorId, StatusExcursao status, Pageable pageable);

    @Query("SELECT e FROM Excursao e WHERE e.organizador.id = :organizadorId AND e.id = :excursaoId")
    Optional<Excursao> findByIdAndOrganizadorId(@Param("excursaoId") UUID excursaoId,
                                                @Param("organizadorId") UUID organizadorId);

    @Query("SELECT e FROM Excursao e WHERE e.status = 'ATIVA' AND e.dataSaida > :now")
    List<Excursao> findExcursoesAtivas(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Excursao e WHERE e.status = 'ATIVA' AND e.dataSaida > :now")
    Page<Excursao> findExcursoesAtivasPageable(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Excursao e WHERE e.organizador.id = :organizadorId AND e.status = :status")
    Long countByOrganizadorIdAndStatus(@Param("organizadorId") UUID organizadorId,
                                       @Param("status") StatusExcursao status);

    @Query("SELECT e FROM Excursao e WHERE e.organizador.id = :organizadorId " +
            "AND e.dataSaida BETWEEN :dataInicio AND :dataFim")
    List<Excursao> findByOrganizadorIdAndDataSaidaBetween(@Param("organizadorId") UUID organizadorId,
                                                          @Param("dataInicio") LocalDateTime dataInicio,
                                                          @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT e FROM Excursao e WHERE e.dataSaida BETWEEN :hoje AND :amanha AND e.status = 'ATIVA'")
    List<Excursao> findExcursoesProximasSaida(@Param("hoje") LocalDateTime hoje,
                                              @Param("amanha") LocalDateTime amanha);

    @Query("SELECT e FROM Excursao e WHERE e.compania.id = :companiaId AND e.status = :status")
    Page<Excursao> findByCompaniaIdAndStatus(@Param("companiaId") UUID companiaId,
                                             @Param("status") StatusExcursao status,
                                             Pageable pageable);

    @Query("SELECT e FROM Excursao e WHERE e.compania.id = :companiaId")
    Page<Excursao> findByCompaniaId(@Param("companiaId") UUID companiaId, Pageable pageable);
}
