package uz.result.moneymanagerbot.bot;

import java.util.concurrent.ConcurrentHashMap;

public class Sessions {

    private static final ConcurrentHashMap<Long, Long> transactionIds = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Long, Long> clientIds = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Long, Integer> serviceIds = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Long, Integer> categoryIds = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Long, String> period = new ConcurrentHashMap<>();

    public static void addTransactionId(Long chatId, Long transactionId) {
        transactionIds.put(chatId, transactionId);
    }

    public static Long getTransactionId(Long chatId) {
        return transactionIds.getOrDefault(chatId, -1L);
    }

    public static void removeTransactionId(Long chatId) {
        transactionIds.remove(chatId);
    }

    public static void addClientId(Long chatId, Long clientId) {
        clientIds.put(chatId, clientId);
    }

    public static Long getClientId(Long chatId) {
        return clientIds.getOrDefault(chatId, -1L);
    }

    public static void removeClientId(Long chatId) {
        clientIds.remove(chatId);
    }

    public static void addServiceId(Long chatId, Integer serviceId) {
        serviceIds.put(chatId, serviceId);
    }

    public static Integer getServiceId(Long chatId) {
        return serviceIds.getOrDefault(chatId, -1);
    }

    public static void removeServiceId(Long chatId) {
        serviceIds.remove(chatId);
    }

    public static void addCategoryId(Long chatId, Integer categoryId) {
        categoryIds.put(chatId, categoryId);
    }

    public static Integer getCategoryId(Long chatId) {
        return categoryIds.getOrDefault(chatId, -1);
    }

    public static void removeCategoryId(Long chatId) {
        categoryIds.remove(chatId);
    }

    public static void addPeriod(Long chatId, String periodText) {
        period.put(chatId, periodText);
    }

    public static String getPeriod(Long chatId) {
        return period.getOrDefault(chatId, " ");
    }

    public static void removePeriod(Long chatId) {
        period.remove(chatId);
    }

    public static void clearSessions() {
        transactionIds.clear();
        serviceIds.clear();
        clientIds.clear();
        categoryIds.clear();
    }

}
