package br.com.tourapp.repository;

import br.com.tourapp.entity.UserEntity;
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
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    // Busca básica
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByGoogleId(String googleId);
    boolean existsByEmail(String email);
    boolean existsByGoogleId(String googleId);

    // Busca por status
    List<UserEntity> findByActiveTrue();
    Page<UserEntity> findByActiveTrue(Pageable pageable);

    // Buscar usuários de uma compania específica
    @Query("SELECT u FROM UserEntity u " +
            "JOIN u.companias uc " +
            "WHERE uc.compania.id = :companiaId AND uc.ativo = true " +
            "ORDER BY uc.roleCompania, u.fullName")
    List<UserEntity> findByCompaniaId(@Param("companiaId") UUID companiaId);

    @Query("SELECT u FROM UserEntity u " +
            "JOIN u.companias uc " +
            "WHERE uc.compania.id = :companiaId AND uc.ativo = true")
    Page<UserEntity> findByCompaniaId(@Param("companiaId") UUID companiaId, Pageable pageable);

    // Buscar clientes (usuários que fazem inscrições)
    @Query("SELECT u FROM UserEntity u " +
            "JOIN u.roles r " +
            "WHERE r.name = 'ROLE_CLIENTE' AND u.active = true")
    List<UserEntity> findClientes();

    // Buscar organizadores (usuários que fazem parte de companias)
    @Query("SELECT DISTINCT u FROM UserEntity u " +
            "JOIN u.companias uc " +
            "WHERE uc.ativo = true AND u.active = true")
    List<UserEntity> findOrganizadores();

    // Buscar usuários com notificações ativas
    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.emailNotifications = true AND u.active = true")
    List<UserEntity> findByEmailNotificationsTrue();

    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.smsNotifications = true AND u.active = true")
    List<UserEntity> findBySmsNotificationsTrue();

    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.pushToken IS NOT NULL AND u.pushToken != '' AND u.active = true")
    List<UserEntity> findByPushTokenNotNull();

    // Buscar clientes inscritos em uma excursão específica
    @Query("SELECT u FROM UserEntity u " +
            "JOIN u.inscricoes i " +
            "WHERE i.excursao.id = :excursaoId")
    List<UserEntity> findByExcursaoId(@Param("excursaoId") UUID excursaoId);

    // Buscar usuários por role global
    @Query("SELECT u FROM UserEntity u " +
            "JOIN u.roles r " +
            "WHERE r.name = :roleName AND u.active = true")
    List<UserEntity> findByRoleName(@Param("roleName") String roleName);

    // Buscar por período de cadastro
    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.createdAt BETWEEN :inicio AND :fim")
    List<UserEntity> findByCreatedAtBetween(@Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);

    // Buscar por último login
    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.lastLogin BETWEEN :inicio AND :fim")
    List<UserEntity> findByLastLoginBetween(@Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);

    // Buscar usuários com assinatura ativa
    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.subscriptionExpiry > :now AND u.active = true")
    List<UserEntity> findWithActiveSubscription(@Param("now") LocalDateTime now);

    // Buscar por cidade/região
    List<UserEntity> findByCidadeAndActiveTrue(String cidade);
    List<UserEntity> findByEstadoAndActiveTrue(String estado);

    // Busca por texto (nome ou email)
    @Query("SELECT u FROM UserEntity u " +
            "WHERE (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :termo, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :termo, '%'))) " +
            "AND u.active = true")
    Page<UserEntity> findByNomeOrEmailContaining(@Param("termo") String termo, Pageable pageable);

    // Estatísticas
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.active = true")
    Long countAtivos();

    @Query("SELECT COUNT(u) FROM UserEntity u " +
            "JOIN u.roles r " +
            "WHERE r.name = :roleName AND u.active = true")
    Long countByRoleName(@Param("roleName") String roleName);

    @Query("SELECT COUNT(DISTINCT u) FROM UserEntity u " +
            "JOIN u.companias uc " +
            "WHERE uc.ativo = true AND u.active = true")
    Long countOrganizadores();

    @Query("SELECT COUNT(u) FROM UserEntity u " +
            "WHERE u.createdAt BETWEEN :inicio AND :fim")
    Long countCreatedBetween(@Param("inicio") LocalDateTime inicio,
                             @Param("fim") LocalDateTime fim);

    // Verificações de negócio
    @Query("SELECT COUNT(i) FROM Inscricao i " +
            "WHERE i.user.id = :userId")
    Long countInscricoesByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(uc) FROM UserCompaniaEntity uc " +
            "WHERE uc.user.id = :userId AND uc.ativo = true")
    Long countCompaniasByUser(@Param("userId") UUID userId);

    // Para relatórios e analytics
    @Query("SELECT u.estado, COUNT(u) FROM UserEntity u " +
            "WHERE u.active = true AND u.estado IS NOT NULL " +
            "GROUP BY u.estado " +
            "ORDER BY COUNT(u) DESC")
    List<Object[]> countByEstado();

    @Query("SELECT u.cidade, COUNT(u) FROM UserEntity u " +
            "WHERE u.active = true AND u.cidade IS NOT NULL " +
            "GROUP BY u.cidade " +
            "ORDER BY COUNT(u) DESC")
    List<Object[]> countByCidade();

    // Buscar usuários inativos há muito tempo
    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.lastLogin < :dataLimite AND u.active = true")
    List<UserEntity> findInactiveUsers(@Param("dataLimite") LocalDateTime dataLimite);

    // Buscar usuários sem perfil completo
    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.active = true AND " +
            "(u.phone IS NULL OR u.phone = '' OR " +
            "u.endereco IS NULL OR u.endereco = '' OR " +
            "u.cidade IS NULL OR u.cidade = '' OR " +
            "u.estado IS NULL OR u.estado = '')")
    List<UserEntity> findUsersWithIncompleteProfile();

    // Para notificações direcionadas
    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.id IN :userIds AND u.emailNotifications = true AND u.active = true")
    List<UserEntity> findByIdsWithEmailNotifications(@Param("userIds") List<UUID> userIds);

    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.id IN :userIds AND u.pushToken IS NOT NULL AND u.active = true")
    List<UserEntity> findByIdsWithPushToken(@Param("userIds") List<UUID> userIds);

    @Query("SELECT u FROM UserEntity u " +
            "WHERE  u.smsNotifications = true AND u.active = true")
    List<UserEntity> findByAtivoTrue();
}