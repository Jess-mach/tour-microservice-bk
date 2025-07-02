package br.com.tourapp.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utilitários para manipulação de datas
 */
public final class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT);

    private DateUtil() {
        // Utility class
    }

    /**
     * Formata LocalDate para string
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * Formata LocalDateTime para string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    /**
     * Parse string para LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr, DATE_FORMATTER) : null;
    }

    /**
     * Parse string para LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER) : null;
    }

    /**
     * Retorna o início do dia
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * Retorna o fim do dia
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59, 999999999) : null;
    }

    /**
     * Calcula a diferença em dias entre duas datas
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return start != null && end != null ? ChronoUnit.DAYS.between(start, end) : 0;
    }

    /**
     * Calcula a diferença em horas entre duas datas
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return start != null && end != null ? ChronoUnit.HOURS.between(start, end) : 0;
    }

    /**
     * Verifica se a data está no futuro
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Verifica se a data está no passado
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Adiciona dias a uma data
     */
    public static LocalDate addDays(LocalDate date, int days) {
        return date != null ? date.plusDays(days) : null;
    }

    /**
     * Adiciona horas a uma data
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, int hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }

    /**
     * Retorna o primeiro dia do mês atual
     */
    public static LocalDate firstDayOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * Retorna o último dia do mês atual
     */
    public static LocalDate lastDayOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(now.lengthOfMonth());
    }
}

