package br.com.tourapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um usuário tenta acessar um recurso sem permissão adequada.
 * Retorna HTTP 403 Forbidden automaticamente.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Acesso negado";

    private String resourceType;
    private String resourceId;
    private String requiredPermission;
    private String userRole;

    /**
     * Construtor padrão com mensagem genérica
     */
    public AccessDeniedException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Construtor com mensagem personalizada
     * @param message Mensagem de erro específica
     */
    public AccessDeniedException(String message) {
        super(message != null && !message.trim().isEmpty() ? message : DEFAULT_MESSAGE);
    }

    /**
     * Construtor com mensagem e causa
     * @param message Mensagem de erro
     * @param cause Causa raiz da exceção
     */
    public AccessDeniedException(String message, Throwable cause) {
        super(message != null && !message.trim().isEmpty() ? message : DEFAULT_MESSAGE, cause);
    }

    /**
     * Construtor detalhado para auditoria e logs
     * @param message Mensagem de erro
     * @param resourceType Tipo do recurso (ex: "compania", "excursao")
     * @param resourceId ID do recurso
     * @param requiredPermission Permissão necessária
     * @param userRole Role atual do usuário
     */
    public AccessDeniedException(String message, String resourceType, String resourceId,
                                 String requiredPermission, String userRole) {
        super(message != null && !message.trim().isEmpty() ? message : DEFAULT_MESSAGE);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.requiredPermission = requiredPermission;
        this.userRole = userRole;
    }

    /**
     * Construtor para negação de acesso a compania
     * @param companiaId ID da compania
     * @param userId ID do usuário
     */
    public static AccessDeniedException companiaAccess(String companiaId, String userId) {
        return new AccessDeniedException(
                "Usuário não tem acesso a esta compania",
                "compania",
                companiaId,
                "ACCESS_COMPANIA",
                null
        );
    }

    /**
     * Construtor para negação de permissão específica
     * @param permission Permissão negada
     * @param resourceType Tipo do recurso
     * @param resourceId ID do recurso
     */
    public static AccessDeniedException permission(String permission, String resourceType, String resourceId) {
        return new AccessDeniedException(
                String.format("Permissão '%s' necessária para acessar %s", permission, resourceType),
                resourceType,
                resourceId,
                permission,
                null
        );
    }

    /**
     * Construtor para operações não permitidas por role
     * @param operation Operação tentada
     * @param currentRole Role atual do usuário
     * @param requiredRole Role necessário
     */
    public static AccessDeniedException insufficientRole(String operation, String currentRole, String requiredRole) {
        return new AccessDeniedException(
                String.format("Operação '%s' requer role '%s', mas usuário possui role '%s'",
                        operation, requiredRole, currentRole),
                "operation",
                operation,
                requiredRole,
                currentRole
        );
    }

    /**
     * Construtor para recursos de outras empresas
     * @param resourceType Tipo do recurso
     * @param resourceId ID do recurso
     */
    public static AccessDeniedException crossCompany(String resourceType, String resourceId) {
        return new AccessDeniedException(
                String.format("Não é possível acessar %s de outra empresa", resourceType),
                resourceType,
                resourceId,
                "SAME_COMPANY",
                null
        );
    }

    /**
     * Construtor para contas inativas/suspensas
     * @param accountStatus Status da conta
     */
    public static AccessDeniedException accountStatus(String accountStatus) {
        return new AccessDeniedException(
                String.format("Conta %s não pode realizar esta operação", accountStatus.toLowerCase()),
                "account",
                null,
                "ACTIVE_ACCOUNT",
                accountStatus
        );
    }

    // ============================================
    // GETTERS E SETTERS
    // ============================================

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    /**
     * Retorna informações detalhadas da exceção para logs
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(getMessage());

        if (resourceType != null) {
            sb.append(" | Recurso: ").append(resourceType);
        }

        if (resourceId != null) {
            sb.append(" (").append(resourceId).append(")");
        }

        if (requiredPermission != null) {
            sb.append(" | Permissão necessária: ").append(requiredPermission);
        }

        if (userRole != null) {
            sb.append(" | Role atual: ").append(userRole);
        }

        return sb.toString();
    }

    /**
     * Retorna se é um erro de acesso a compania
     */
    public boolean isCompaniaAccessError() {
        return "compania".equals(resourceType) && "ACCESS_COMPANIA".equals(requiredPermission);
    }

    /**
     * Retorna se é um erro de role insuficiente
     */
    public boolean isInsufficientRoleError() {
        return userRole != null && requiredPermission != null && !userRole.equals(requiredPermission);
    }

    /**
     * Retorna se é um erro de acesso cross-company
     */
    public boolean isCrossCompanyError() {
        return "SAME_COMPANY".equals(requiredPermission);
    }

    @Override
    public String toString() {
        return String.format("AccessDeniedException{message='%s', resourceType='%s', resourceId='%s', " +
                        "requiredPermission='%s', userRole='%s'}",
                getMessage(), resourceType, resourceId, requiredPermission, userRole);
    }
}