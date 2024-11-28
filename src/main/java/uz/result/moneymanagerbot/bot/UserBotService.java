package uz.result.moneymanagerbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import uz.result.moneymanagerbot.model.UserState;
import uz.result.moneymanagerbot.service.*;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserBotService {

    private final UserService userService;

    private final MarkupService markupService;

    private final TransactionService transactionService;

    private final ExpenseCategoryService expenseCategoryService;

    private final ValidationService validationService;

    private final ClientService clientService;

    private final FileService fileService;

    private final ServiceTypeService serviceTypeService;

    private final NotificationService notificationService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${bot.token}")
    private String token;

    @Value("${photos.files.file.path}")
    private String photoFilePath;

    @SneakyThrows
    public void fromMenuToBaseMenuHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*\uD83D\uDEE1 Вы можете полностью использовать функции пользователя*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.transactionTypeReplyMarkupForUser());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADD_TRANSACTION);
    }

    @SneakyThrows
    private void baseMenuHandler(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*\uD83D\uDEE1 Вы можете полностью воспользоваться возможностями администратора*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setReplyMarkup(markupService.transactionTypeReplyMarkupForUser());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADD_TRANSACTION);
    }

    @SneakyThrows
    public void passwordStateHandler(Long chatId, String password, Integer messageId, TelegramWebhookBot bot) {
        String dbPassword = userService.findByChatId(chatId).getPassword();
        if (!dbPassword.equals(password)) {
            warningMessageForWrongPassword(chatId, messageId, bot);
            return;
        }
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*Подтверждено✅*");
        sendMessage.setParseMode("Markdown");

        if (messageId != null) {
            sendMessage.setReplyToMessageId(messageId);
        }

        Integer confirmMessageId = bot.execute(sendMessage).getMessageId();
        userService.signIn(chatId);
        baseMenuHandler(chatId, confirmMessageId, bot);
    }

    @SneakyThrows
    private void warningMessageForWrongPassword(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(),
                "*Введенный вами пароль неверный❌\n" +
                        "Попробуйте снова\uD83D\uDD04*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyToMessageId(messageId);
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void menuHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*\uD83D\uDEE1 Вы можете полностью использовать возможности администратора*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.baseMenuReplyMarkupServiceForUser());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.BASE_MENU);
    }

    @SneakyThrows
    public void baseMenuForBackHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*\uD83D\uDEE1 Вы можете полностью использовать возможности администратора*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.baseMenuReplyMarkupServiceForUser());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.BASE_MENU);
    }
}
