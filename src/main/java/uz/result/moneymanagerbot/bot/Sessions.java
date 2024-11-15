package uz.result.moneymanagerbot.bot;

import java.util.concurrent.ConcurrentHashMap;

public class Sessions {

    private static final ConcurrentHashMap<Long, Long> transactionIds = new ConcurrentHashMap<>();

    public static void addTransactionId(Long chatId, Long transactionId) {
        transactionIds.put(chatId, transactionId);
    }

    public static Long getTransactionId(Long chatId) {
        return transactionIds.getOrDefault(chatId, null);
    }

    public static void removeTransactionId(Long chatId) {
        transactionIds.remove(chatId);
    }

}
