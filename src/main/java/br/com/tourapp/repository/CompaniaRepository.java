package br.com.tourapp.repository;

import br.com.tourapp.dto.enums.StatusCompania;
import br.com.tourapp.entity.CompaniaEntity;
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
public interface CompaniaRepository extends JpaRepository<CompaniaEntity, UUID> {

    // Busca básica
    Optional<CompaniaEntity> findByCnpj(String cnpj);
    boolean existsByCnpj(String cnpj);
    boolean existsByNomeEmpresa(String nomeEmpresa);

    // Busca por status
    List<CompaniaEntity> findByStatus(StatusCompania status);
    Page<CompaniaEntity> findByStatus(StatusCompania status, Pageable pageable);

    // Buscar companias por usuário
    @Query("SELECT c FROM CompaniaEntity c " +
            "JOIN c.usuarios uc " +
            "WHERE uc.user.id = :userId AND uc.ativo = true")
    List<CompaniaEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM CompaniaEntity c " +
            "JOIN c.usuarios uc " +
            "WHERE uc.user.id = :userId AND uc.ativo = true AND c.status = :status")
    List<CompaniaEntity> findByUserIdAndStatus(@Param("userId") UUID userId,
                                               @Param("status") StatusCompania status);

    // Buscar companias ativas onde o usuário é admin
    @Query("SELECT c FROM CompaniaEntity c " +
            "JOIN c.usuarios uc " +
            "WHERE uc.user.id = :userId AND uc.ativo = true " +
            "AND uc.roleCompania = 'ADMIN' AND c.status = 'ATIVA'")
    List<CompaniaEntity> findCompaniasAdminByUserId(@Param("userId") UUID userId);

    // Verificar se usuário tem acesso à compania
    @Query("SELECT COUNT(uc) > 0 FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.compania.id = :companiaId " +
            "AND uc.ativo = true")
    boolean existsUserInCompania(@Param("userId") UUID userId,
                                 @Param("companiaId") UUID companiaId);

    // Estatísticas
    @Query("SELECT COUNT(c) FROM CompaniaEntity c WHERE c.status = :status")
    Long countByStatus(@Param("status") StatusCompania status);

    @Query("SELECT COUNT(c) FROM CompaniaEntity c WHERE c.createdAt BETWEEN :inicio AND :fim")
    Long countCreatedBetween(@Param("inicio") LocalDateTime inicio,
                             @Param("fim") LocalDateTime fim);

    // Buscar companias com mais excursões
    @Query("SELECT c FROM CompaniaEntity c " +
            "LEFT JOIN c.excursoes e " +
            "WHERE c.status = 'ATIVA' " +
            "GROUP BY c " +
            "ORDER BY COUNT(e) DESC")
    Page<CompaniaEntity> findCompaniasComMaisExcursoes(Pageable pageable);

    // Buscar companias por cidade/estado
    List<CompaniaEntity> findByCidadeAndStatus(String cidade, StatusCompania status);
    List<CompaniaEntity> findByEstadoAndStatus(String estado, StatusCompania status);

    // Busca por texto (nome da empresa)
    @Query("SELECT c FROM CompaniaEntity c " +
            "WHERE LOWER(c.nomeEmpresa) LIKE LOWER(CONCAT('%', :termo, '%')) " +
            "AND c.status = 'ATIVA'")
    Page<CompaniaEntity> findByNomeContainingIgnoreCase(@Param("termo") String termo,
                                                        Pageable pageable);

    // Buscar companias pendentes de aprovação
    @Query("SELECT c FROM CompaniaEntity c " +
            "WHERE c.status = 'PENDENTE_APROVACAO' " +
            "ORDER BY c.createdAt ASC")
    List<CompaniaEntity> findPendentesAprovacao();

    // Buscar companias por região
    @Query("SELECT c FROM CompaniaEntity c " +
            "WHERE c.estado IN :estados AND c.status = 'ATIVA'")
    List<CompaniaEntity> findByEstadosAndAtiva(@Param("estados") List<String> estados);

    // Verificações de negócio
    @Query("SELECT COUNT(uc) FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.ativo = true")
    Long countUsuariosAtivos(@Param("companiaId") UUID companiaId);

    @Query("SELECT COUNT(e) FROM Excursao e " +
            "WHERE e.compania.id = :companiaId AND e.status = 'ATIVA'")
    Long countExcursoesAtivas(@Param("companiaId") UUID companiaId);

    // Para relatórios e dashboard
    @Query("SELECT c, COUNT(e) as totalExcursoes " +
            "FROM CompaniaEntity c " +
            "LEFT JOIN c.excursoes e " +
            "WHERE c.status = 'ATIVA' " +
            "GROUP BY c " +
            "HAVING COUNT(e) > 0")
    List<Object[]> findCompaniasComEstatisticas();
}