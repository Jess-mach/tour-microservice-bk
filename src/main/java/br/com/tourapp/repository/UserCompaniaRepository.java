package br.com.tourapp.repository;

import br.com.tourapp.dto.enums.RoleCompania;
import br.com.tourapp.entity.UserCompaniaEntity;
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
public interface UserCompaniaRepository extends JpaRepository<UserCompaniaEntity, UUID> {

    // Busca básica do relacionamento
    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.compania.id = :companiaId AND uc.ativo = true")
    Optional<UserCompaniaEntity> findByUserAndCompania(@Param("userId") UUID userId,
                                                       @Param("companiaId") UUID companiaId);

    // Buscar todas as companias de um usuário
    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.ativo = true " +
            "ORDER BY uc.dataIngresso DESC")
    List<UserCompaniaEntity> findByUserIdAndAtivoTrue(@Param("userId") UUID userId);

    // Buscar todos os usuários de uma compania
    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.ativo = true " +
            "ORDER BY uc.roleCompania, uc.dataIngresso DESC")
    List<UserCompaniaEntity> findByCompaniaIdAndAtivoTrue(@Param("companiaId") UUID companiaId);

    // Paginado
    Page<UserCompaniaEntity> findByCompaniaIdAndAtivoTrue(UUID companiaId, Pageable pageable);

    // Verificar se relacionamento existe e está ativo
    boolean existsByUserIdAndCompaniaIdAndAtivoTrue(UUID userId, UUID companiaId);

    // Buscar por usuário e role específico
    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.roleCompania = :role AND uc.ativo = true")
    List<UserCompaniaEntity> findByUserAndRole(@Param("userId") UUID userId,
                                               @Param("role") RoleCompania role);

    // Buscar por compania e role específico
    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.roleCompania = :role AND uc.ativo = true")
    List<UserCompaniaEntity> findByCompaniaAndRole(@Param("companiaId") UUID companiaId,
                                                   @Param("role") RoleCompania role);

    // Buscar administradores de uma compania
    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.roleCompania = 'ADMIN' AND uc.ativo = true")
    List<UserCompaniaEntity> findAdminsByCompaniaId(@Param("companiaId") UUID companiaId);

    // Verificações de permissão
    @Query("SELECT uc.podeCreiarExcursoes FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.compania.id = :companiaId AND uc.ativo = true")
    Optional<Boolean> canCreateExcursoes(@Param("userId") UUID userId,
                                         @Param("companiaId") UUID companiaId);

    @Query("SELECT uc.podeGerenciarUsuarios FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.compania.id = :companiaId AND uc.ativo = true")
    Optional<Boolean> canManageUsers(@Param("userId") UUID userId,
                                     @Param("companiaId") UUID companiaId);

    @Query("SELECT uc.podeVerFinanceiro FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.compania.id = :companiaId AND uc.ativo = true")
    Optional<Boolean> canViewFinancial(@Param("userId") UUID userId,
                                       @Param("companiaId") UUID companiaId);

    @Query("SELECT uc.podeEditarCompania FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.compania.id = :companiaId AND uc.ativo = true")
    Optional<Boolean> canEditCompania(@Param("userId") UUID userId,
                                      @Param("companiaId") UUID companiaId);

    // Estatísticas
    @Query("SELECT COUNT(uc) FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.ativo = true")
    Long countActiveUsersByCompania(@Param("companiaId") UUID companiaId);

    @Query("SELECT COUNT(uc) FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.roleCompania = :role AND uc.ativo = true")
    Long countByCompaniaAndRole(@Param("companiaId") UUID companiaId,
                                @Param("role") RoleCompania role);

    @Query("SELECT COUNT(uc) FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.ativo = true")
    Long countActiveCompaniesByUser(@Param("userId") UUID userId);

    // Buscar convites pendentes
    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.convidadoPor IS NOT NULL AND uc.dataAceiteConvite IS NULL " +
            "AND uc.ativo = true")
    List<UserCompaniaEntity> findPendingInvites();

    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.convidadoPor IS NOT NULL " +
            "AND uc.dataAceiteConvite IS NULL AND uc.ativo = true")
    List<UserCompaniaEntity> findPendingInvitesByUser(@Param("userId") UUID userId);

    // Buscar por período de ingresso
    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.dataIngresso BETWEEN :inicio AND :fim " +
            "AND uc.ativo = true")
    List<UserCompaniaEntity> findByCompaniaAndDataIngressoBetween(@Param("companiaId") UUID companiaId,
                                                                  @Param("inicio") LocalDateTime inicio,
                                                                  @Param("fim") LocalDateTime fim);

    // Verificar se usuário é único admin da compania
    @Query("SELECT COUNT(uc) FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.roleCompania = 'ADMIN' " +
            "AND uc.ativo = true AND uc.user.id != :userId")
    Long countOtherAdminsByCompania(@Param("companiaId") UUID companiaId,
                                    @Param("userId") UUID userId);

    // Buscar histórico (incluindo inativos)
    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId " +
            "ORDER BY uc.ativo DESC, uc.dataIngresso DESC")
    List<UserCompaniaEntity> findHistoryByCompaniaId(@Param("companiaId") UUID companiaId);

    // Para relatórios
    @Query("SELECT uc.roleCompania, COUNT(uc) FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.ativo = true " +
            "GROUP BY uc.roleCompania")
    List<Object[]> countByRoleInCompania(@Param("companiaId") UUID companiaId);

    // Buscar usuários com permissões específicas
    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.podeCreiarExcursoes = true " +
            "AND uc.ativo = true")
    List<UserCompaniaEntity> findUsersCanCreateExcursoes(@Param("companiaId") UUID companiaId);

    @Query("SELECT uc FROM UserCompaniaEntity uc " +
            "WHERE uc.compania.id = :companiaId AND uc.podeEnviarNotificacoes = true " +
            "AND uc.ativo = true")
    List<UserCompaniaEntity> findUsersCanSendNotifications(@Param("companiaId") UUID companiaId);
}