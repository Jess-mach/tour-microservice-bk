package br.com.tourapp.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilitários para manipulação de strings
 */
public final class StringUtil {

    private StringUtil() {
        // Utility class
    }

    /**
     * Verifica se a string é nula ou vazia
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Verifica se a string não é nula e não está vazia
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Remove acentos de uma string
     */
    public static String removeAccents(String str) {
        if (isEmpty(str)) {
            return str;
        }

        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    /**
     * Converte para slug (URL-friendly)
     */
    public static String toSlug(String str) {
        if (isEmpty(str)) {
            return "";
        }

        return removeAccents(str)
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Capitaliza a primeira letra
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Capitaliza cada palavra
     */
    public static String capitalizeWords(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return Arrays.stream(str.split("\\s+"))
                .map(StringUtil::capitalize)
                .collect(Collectors.joining(" "));
    }

    /**
     * Trunca string com reticências
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }

        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Máscara para CPF
     */
    public static String maskCpf(String cpf) {
        if (isEmpty(cpf)) {
            return cpf;
        }

        String digits = cpf.replaceAll("[^0-9]", "");
        if (digits.length() != 11) {
            return cpf;
        }

        return digits.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    /**
     * Máscara para CNPJ
     */
    public static String maskCnpj(String cnpj) {
        if (isEmpty(cnpj)) {
            return cnpj;
        }

        String digits = cnpj.replaceAll("[^0-9]", "");
        if (digits.length() != 14) {
            return cnpj;
        }

        return digits.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    /**
     * Máscara para telefone
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone)) {
            return phone;
        }

        String digits = phone.replaceAll("[^0-9]", "");

        if (digits.length() == 11) { // Celular
            return digits.replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        } else if (digits.length() == 10) { // Fixo
            return digits.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
        }

        return phone;
    }

    /**
     * Gera uma string aleatória
     */
    public static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    /**
     * Converte lista de strings em string separada por vírgulas
     */
    public static String join(List<String> list, String delimiter) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        return String.join(delimiter, list);
    }

    /**
     * Converte string separada por vírgulas em lista
     */
    public static List<String> split(String str, String delimiter) {
        if (isEmpty(str)) {
            return List.of();
        }

        return Arrays.stream(str.split(delimiter))
                .map(String::trim)
                .filter(StringUtil::isNotEmpty)
                .collect(Collectors.toList());
    }
}