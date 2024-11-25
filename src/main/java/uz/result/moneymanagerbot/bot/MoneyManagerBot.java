package uz.result.moneymanagerbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.result.moneymanagerbot.model.Notification;
import uz.result.moneymanagerbot.model.UserRole;
import uz.result.moneymanagerbot.service.NotificationService;
import uz.result.moneymanagerbot.service.UserService;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class MoneyManagerBot extends TelegramWebhookBot {

    private final UserService userService;

    private final HandlerService handlerService;

    private final AdminBotService adminBotService;

    private final NotificationService notificationService;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    @Value("${bot.webhook.path}")
    private String webhookPath;

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotPath() {
        return webhookPath;
    }

    @SneakyThrows
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            UserRole currentUserRole = userService.findRoleByChatId(chatId);

            if (currentUserRole.equals(UserRole.SUPPER_ADMIN) || currentUserRole.equals(UserRole.ADMIN)) {
                handlerService.handleAdminRole(chatId, message, this);
            } else if (currentUserRole.equals(UserRole.OBSERVER)) {
                handlerService.handleUserRole(chatId, message, this);
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            Long chatId = callbackQuery.getMessage().getChatId();
            String data = callbackQuery.getData();
            UserRole currentUserRole = userService.findRoleByChatId(chatId);

            if (currentUserRole.equals(UserRole.SUPPER_ADMIN) || currentUserRole.equals(UserRole.ADMIN)) {
                handlerService.handleAdminCallbackQuery(chatId, data, callbackQuery, this);
            } else if (currentUserRole.equals(UserRole.OBSERVER)) {
                handlerService.handleUserCallbackQuery(chatId, data, callbackQuery, this);
            }
        }
        return null;
    }

    @SneakyThrows
    @Scheduled(fixedRate = 60000)
    public void notification() {
        for (Notification notification : notificationService.processNotifications()) {
            String text = "*Уведомление*\n\n" +
                    "*Дата: *" + notification.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                    "*Текст: *" + notification.getMessage() + "\n" +
                    "*Сумма транзакции: *" + notification.getSumma() + "\n" +
                    "*Тип транзакции: *" + adminBotService.showTransactionType(notification.getType()) + "\n" +
                    "*Тип уведомления: *" + adminBotService.showNotificationType(notification.getRepeatInterval()) + "\n";
            SendMessage sendMessage = new SendMessage(notification.getChatId().toString(), text);
            sendMessage.setParseMode("Markdown");
            execute(sendMessage);
        }
    }

}
