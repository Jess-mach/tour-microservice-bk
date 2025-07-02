package br.com.tourapp.tourapp.util;

/**
 * Constantes da aplicação
 */
public final class Constants {

    // Roles de usuário
    public static final String ROLE_CLIENTE = "ROLE_CLIENTE";
    public static final String ROLE_ORGANIZADOR = "ROLE_ORGANIZADOR";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // Headers HTTP
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // Formatos de data
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    // Limites de arquivo
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/webp"};

    // Configurações de paginação
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Configurações de cache
    public static final int CACHE_TTL_SECONDS = 3600; // 1 hora
    public static final String CACHE_EXCURSOES = "excursoes";
    public static final String CACHE_DASHBOARD = "dashboard";

    // Templates de email
    public static final String EMAIL_CONFIRMACAO_INSCRICAO = "email/confirmacao-inscricao";
    public static final String EMAIL_CONFIRMACAO_PAGAMENTO = "email/confirmacao-pagamento";
    public static final String EMAIL_NOTIFICACAO_PERSONALIZADA = "email/notificacao-personalizada";

    // Mensagens padrão
    public static final String MSG_RECURSO_NAO_ENCONTRADO = "Recurso não encontrado";
    public static final String MSG_ACESSO_NEGADO = "Acesso negado";
    public static final String MSG_ERRO_INTERNO = "Erro interno do servidor";
    public static final String MSG_VALIDACAO_ERRO = "Erro de validação";

    // Status de resposta
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_WARNING = "warning";

    // Regex patterns
    public static final String PHONE_PATTERN = "^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}[\\s-]?\\d{4}$";
    public static final String CPF_PATTERN = "^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$";
    public static final String CNPJ_PATTERN = "^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$";

    private Constants() {
        // Utility class - não instanciar
    }
}

