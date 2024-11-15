package uz.result.moneymanagerbot.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.result.moneymanagerbot.model.MoneyType;
import uz.result.moneymanagerbot.model.UserState;
import uz.result.moneymanagerbot.service.UserService;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class HandlerService {

    private final UserService userService;

    private final AdminBotService adminBotService;

    public void handleUserRole(Long chatId, Message message, TelegramWebhookBot bot) {

    }

    public void handleUserCallbackQuery(Long chatId, String data, CallbackQuery callbackQuery, TelegramWebhookBot bot) {

    }

    public void handleAdminRole(Long chatId, Message message, TelegramWebhookBot bot) {
        UserState currentState = userService.findStateByChatId(chatId);
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals("/start")) {
                if (!currentState.equals(UserState.DEFAULT)) {
                    userService.updateStateByChatId(chatId, UserState.START);
                    currentState = userService.findStateByChatId(chatId);
                }
            }
            switch (currentState) {
                case DEFAULT -> adminBotService.defaultStateHandler(chatId, text, message, bot);
                case START -> adminBotService.startStateHandler(chatId, bot);
                case PASSWORD -> adminBotService.passwordStateHandler(chatId, text, message.getMessageId(), bot);
                case TRANSACTION_SUMMA ->
                        adminBotService.transactionSummaStateHandler(chatId, text, message.getMessageId(), bot);
                case OTHER_DATE -> adminBotService.otherDateStateHandler(chatId, text, bot);
                case COMMENT -> adminBotService.commentStateHandler(chatId, text, bot);
                case TRANSACTION_DATE -> adminBotService.transactionDateMessageHandler(chatId, bot);
                case BASE_MENU -> {
                    switch (text) {
                        case "➕Добавить транзакцию" -> adminBotService.addTransactionHandler(chatId, bot);
                    }
                }
                case ADD_TRANSACTION -> {
                    switch (text) {
                        case "Доход", "Расход" -> adminBotService.incomeMessageHandler(chatId, text, bot);
                        case "Перемещение" -> {

                        }
                        case "Назад\uD83D\uDD19" -> adminBotService.baseMenuForBackHandler(chatId, bot);
                    }
                }
            }

        }
        if (message.hasDocument()) {
            Document document = message.getDocument();
            if (currentState.equals(UserState.REQUEST_TRANSACTION_FILE))
                adminBotService.requestTransactionFileStateHandler(chatId, document, message.getMessageId(), bot);
        }
    }

    public void handleAdminCallbackQuery(Long chatId, String data, CallbackQuery callbackQuery, TelegramWebhookBot bot) throws IOException {
        UserState currentState = userService.findStateByChatId(chatId);
        switch (currentState) {
            case MONEY_TYPE -> {
                String transactionId = "";
                if (data.startsWith(MoneyType.CASH_AMOUNT.name())) {
                    transactionId = data.substring(12);
                    data = MoneyType.CASH_AMOUNT.name();
                } else if (data.startsWith(MoneyType.CASH_CURRENCY.name())) {
                    transactionId = data.substring(14);
                    data = MoneyType.CASH_CURRENCY.name();
                } else if (data.startsWith(MoneyType.BANK.name())) {
                    transactionId = data.substring(5);
                    data = MoneyType.BANK.name();
                } else if (data.startsWith(MoneyType.CARD_AMOUNT1.name())) {
                    transactionId = data.substring(13);
                    data = MoneyType.CARD_AMOUNT1.name();
                } else if (data.startsWith(MoneyType.CARD_AMOUNT_2.name())) {
                    transactionId = data.substring(14);
                    data = MoneyType.CARD_AMOUNT_2.name();
                } else if (data.startsWith(MoneyType.CARD_AMOUNT_3.name())) {
                    transactionId = data.substring(14);
                    data = MoneyType.CARD_AMOUNT_3.name();
                } else if (data.startsWith(MoneyType.VISA.name())) {
                    transactionId = data.substring(5);
                    data = MoneyType.VISA.name();
                } else if (data.startsWith("back_")) {
                    transactionId = data.substring(5);
                    data = "back";
                }
                switch (data) {
                    case "back" -> adminBotService.addTransactionHandler(chatId, bot);
                    case "CASH_AMOUNT", "CASH_CURRENCY", "BANK", "CARD_AMOUNT1", "CARD_AMOUNT_2", "CARD_AMOUNT_3", "VISA" ->
                            adminBotService.moneyTypeStateHandler(chatId, data, transactionId, bot);
                }
            }
            case TRANSACTION_DATE -> {
                switch (data) {
                    case "now" -> adminBotService.transactionDateNow(chatId, bot);
                    case "other" -> adminBotService.otherDateHandler(chatId, bot);
                }
            }
            case EXPENSE_TYPE -> {
                if (data.equals("other")) {
                    adminBotService.otherExpenseTypeHandler(chatId, bot);
                } else {
                    adminBotService.expenseTypeStateHandler(chatId, data, bot);
                }
            }
            case INCOME_TYPE -> {
                if (data.equals("other")) {
                    adminBotService.otherIncomeTypeHandler(chatId, bot);
                } else {
                    adminBotService.incomeTypeStateHandler(chatId, data, bot);
                }
            }
            case COMMENT_REQUEST -> {
                switch (data) {
                    case "yes" -> adminBotService.commentYesStateHandler(chatId, bot);
                    case "no" -> adminBotService.commentNoStateHandler(chatId, bot);
                }
            }
            case TRANSACTION_FILE -> {
                switch (data) {
                    case "yes" -> adminBotService.transactionFileYesStateHandler(chatId, bot);
                    case "no" -> adminBotService.transactionFileNoStateHandler(chatId, bot);
                }
            }
            case TRANSACTION_CONFIRM_REQUEST -> {
                switch (data) {
                    case "confirm" -> adminBotService.transactionConfirmHandler(chatId, bot);
                    case "cancellation" -> adminBotService.transactionCancellationHandler(chatId, bot);
                }
            }
            case INCOME_TYPE_STATUS -> adminBotService.incomeTypeStatusStateHandler(chatId,data,bot);
            }
        }
    }


