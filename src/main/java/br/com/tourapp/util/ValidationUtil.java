package br.com.tourapp.util;

import java.util.regex.Pattern;

/**
 * Utilitários para validação
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(Constants.PHONE_PATTERN);
    private static final Pattern CPF_PATTERN = Pattern.compile(Constants.CPF_PATTERN);
    private static final Pattern CNPJ_PATTERN = Pattern.compile(Constants.CNPJ_PATTERN);

    private ValidationUtil() {
        // Utility class
    }

    /**
     * Valida se o email tem formato válido
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valida se o telefone tem formato válido
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Valida se o CPF tem formato válido (apenas formato, não dígitos)
     */
    public static boolean isValidCpfFormat(String cpf) {
        return cpf != null && CPF_PATTERN.matcher(cpf).matches();
    }

    /**
     * Valida se o CNPJ tem formato válido (apenas formato, não dígitos)
     */
    public static boolean isValidCnpjFormat(String cnpj) {
        return cnpj != null && CNPJ_PATTERN.matcher(cnpj).matches();
    }

    /**
     * Remove caracteres especiais de CPF/CNPJ
     */
    public static String removeSpecialCharacters(String value) {
        return value != null ? value.replaceAll("[^0-9]", "") : null;
    }

    /**
     * Valida se a senha atende aos critérios mínimos
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        // Pelo menos uma letra e um número
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");

        return hasLetter && hasNumber;
    }

    /**
     * Valida se o tipo de arquivo é uma imagem permitida
     */
    public static boolean isValidImageType(String contentType) {
        if (contentType == null) {
            return false;
        }

        for (String allowedType : Constants.ALLOWED_IMAGE_TYPES) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Valida se o tamanho do arquivo está dentro do limite
     */
    public static boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= Constants.MAX_FILE_SIZE;
    }
}

