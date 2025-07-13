package br.com.tourapp.repository;

import br.com.tourapp.entity.Inscricao;
import br.com.tourapp.enums.StatusPagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface InscricaoRepository extends JpaRepository<Inscricao, UUID> {

    @Query("SELECT i FROM Inscricao i WHERE i.user.id = :userId")
    Page<Inscricao> findByClienteId(@Param("userId") UUID userId, Pageable pageable);

    Page<Inscricao> findByExcursaoId(UUID excursaoId, Pageable pageable);

    @Query("SELECT i FROM Inscricao i WHERE i.user.id = :userId AND i.id = :inscricaoId")
    Optional<Inscricao> findByIdAndClienteId(@Param("inscricaoId") UUID inscricaoId,
                                             @Param("userId") UUID userId);

    @Query("SELECT i FROM Inscricao i WHERE i.excursao.organizador.id = :organizadorId")
    Page<Inscricao> findByOrganizadorId(@Param("organizadorId") UUID organizadorId, Pageable pageable);

    @Query("SELECT i FROM Inscricao i WHERE i.excursao.organizador.id = :organizadorId " +
            "AND (:excursaoId IS NULL OR i.excursao.id = :excursaoId)")
    Page<Inscricao> findByOrganizadorIdAndExcursaoId(@Param("organizadorId") UUID organizadorId,
                                                     @Param("excursaoId") UUID excursaoId,
                                                     Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
            "FROM Inscricao i WHERE i.user.id = :userId AND i.excursao.id = :excursaoId")
    boolean existsByClienteIdAndExcursaoId(@Param("userId") UUID userId, @Param("excursaoId") UUID excursaoId);

    @Query("SELECT COUNT(i) FROM Inscricao i WHERE i.excursao.id = :excursaoId AND i.statusPagamento = 'APROVADO'")
    Long countInscricoesAprovadasByExcursaoId(@Param("excursaoId") UUID excursaoId);

    @Query("SELECT SUM(i.valorPago) FROM Inscricao i WHERE i.excursao.organizador.id = :organizadorId " +
            "AND i.statusPagamento = 'APROVADO' AND i.createdAt BETWEEN :dataInicio AND :dataFim")
    BigDecimal findTotalReceitaByOrganizadorIdAndPeriodo(@Param("organizadorId") UUID organizadorId,
                                                         @Param("dataInicio") LocalDateTime dataInicio,
                                                         @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT i FROM Inscricao i WHERE i.statusPagamento = :status")
    List<Inscricao> findByStatusPagamento(@Param("status") StatusPagamento status);

    @Query("SELECT SUM(i.valorPago) FROM Inscricao i " +
            "WHERE i.excursao.compania.id = :companiaId " +
            "AND i.statusPagamento = 'APROVADO' " +
            "AND i.createdAt BETWEEN :inicio AND :fim")
    BigDecimal findTotalReceitaByCompaniaAndPeriodo(@Param("companiaId") UUID companiaId,
                                                    @Param("inicio") LocalDateTime inicio,
                                                    @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(i) FROM Inscricao i " +
            "WHERE i.excursao.compania.id = :companiaId " +
            "AND i.createdAt BETWEEN :inicio AND :fim")
    Long countInscricoesByCompaniaAndPeriodo(@Param("companiaId") UUID companiaId,
                                             @Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim);
}
