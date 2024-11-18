package uz.result.moneymanagerbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.result.moneymanagerbot.model.*;
import uz.result.moneymanagerbot.service.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminBotService {

    private final UserService userService;

    private final MarkupService markupService;

    private final TransactionService transactionService;

    private final ExpenseCategoryService expenseCategoryService;

    private final ValidationService validationService;

    private final ClientService clientService;

    private final FileService fileService;

    private final ServiceTypeService serviceTypeService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${bot.token}")
    private String token;

    @Value("${photos.files.file.path}")
    private String photoFilePath;

    @Value("${photos.bot.file.path}")
    private String photoBotPath;


    @SneakyThrows
    public void defaultStateHandler(Long chatId, String text, Message message, TelegramWebhookBot bot) {
        if (!text.equals("/start")) {
            warningMessageForStartText(chatId, message.getMessageId(), bot);
            return;
        }

        String firstName = checkWordIsNull(message.getFrom().getFirstName());
        String lastName = checkWordIsNull(message.getFrom().getLastName());
        SendMessage sendMessage = new SendMessage(chatId.toString(),
                "*\uD83D\uDC4B Здравствуйте, " + firstName + " " + lastName + "!\n\n" +
                        "\uD83C\uDF1F У вас есть права администратора в вашем аккаунте.\n" +
                        "⚠️ Для активации функций администратора необходимо ввести пароль*"
        );
        sendMessage.setParseMode("Markdown");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.START);
        startStateHandler(chatId, bot);
    }

    private String checkWordIsNull(String word) {
        if (word == null)
            return "";
        return word;
    }

    @SneakyThrows
    private void warningMessageForStartText(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(),
                "Для запуска бота введите команду /start"
        );
        sendMessage.setReplyToMessageId(messageId);
        bot.execute(sendMessage);
    }


    @SneakyThrows
    public void startStateHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*Введите ваш пароль: *");
        sendMessage.setParseMode("Markdown");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.PASSWORD);
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

        // Javob berish uchun messageId mavjudligini tekshirish
        if (messageId != null) {
            sendMessage.setReplyToMessageId(messageId);
        }

        Integer confirmMessageId = bot.execute(sendMessage).getMessageId();
        baseMenuHandler(chatId, confirmMessageId, bot);
    }

    @SneakyThrows
    private void baseMenuHandler(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*\uD83D\uDEE1 Вы можете полностью воспользоваться возможностями администратора*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setReplyMarkup(markupService.baseMenuReplyMarkupService());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.BASE_MENU);
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
    public void addTransactionHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите тип транзакции");
        sendMessage.setReplyMarkup(markupService.transactionTypeReplyMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADD_TRANSACTION);
    }

    @SneakyThrows
    public void baseMenuForBackHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*\uD83D\uDEE1 Вы можете полностью использовать возможности администратора*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.baseMenuReplyMarkupService());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.BASE_MENU);
    }

    @SneakyThrows
    public void incomeMessageHandler(Long chatId, String text, TelegramWebhookBot bot) {
        TransactionType transactionType = getTransactionType(text);
        Transaction transaction = Transaction.builder().transactionType(transactionType).build();
        Long transactionId = transactionService.save(transaction).getId();
        trickMessageForIncomeMessage(chatId, bot);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Валюты");
        sendMessage.setReplyMarkup(markupService.moneyTypeListInlineMarkup(transactionId));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.MONEY_TYPE);
    }

    @SneakyThrows
    private void trickMessageForIncomeMessage(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*Выберите валюту для транзакции*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    private TransactionType getTransactionType(String text) {
        if (text.equals("Доход"))
            return TransactionType.INCOME;
        if (text.equals("Расход"))
            return TransactionType.EXPENSE;
        if (text.equals("Перемещение"))
            return TransactionType.TRANSFER;
        return null;
    }

    @SneakyThrows
    public void moneyTypeStateHandler(Long chatId, String moneyType, String transactionId, TelegramWebhookBot bot) {
        transactionService.updateTransactionMoneyTypeById(Long.valueOf(transactionId), moneyType);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите сумму транзакции (Только числа): ");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.TRANSACTION_SUMMA);
        Sessions.addTransactionId(chatId, Long.valueOf(transactionId));
    }

    @SneakyThrows
    public void transactionSummaStateHandler(Long chatId, String transactionSumma, Integer messageId, TelegramWebhookBot bot) {
        if (!validationService.isValidDouble(transactionSumma)) {
            warningMessageForPriceHandler(chatId, messageId, bot);
            return;
        }
        transactionService.updateTransactionSummaById(Sessions.getTransactionId(chatId), Double.valueOf(transactionSumma));
        confirmMessageForTranSumma(chatId, messageId, bot);
        transactionDateMessageHandler(chatId, bot);
    }

    @SneakyThrows
    public void transactionDateMessageHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите дату транзакции: ");
        sendMessage.setReplyMarkup(markupService.transactionDateInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.TRANSACTION_DATE);
    }

    @SneakyThrows
    private void confirmMessageForTranSumma(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Принято✅");
        sendMessage.setReplyToMessageId(messageId);
        bot.execute(sendMessage);
    }

    @SneakyThrows
    private void warningMessageForPriceHandler(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        String text = "⚠️ *Введите сумму транзакции в указанном формате, то есть только из цифр:* ";
        sendMessage.setText(text);
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyToMessageId(messageId);
        bot.execute(sendMessage);
    }

    public void transactionDateNow(Long chatId, TelegramWebhookBot bot) {
        LocalDate date = LocalDate.now();
        transactionService.updateTransactionDateById(Sessions.getTransactionId(chatId), date);
        incomeOrExpenseRequestFormHandler(chatId, bot);
    }

    private void incomeOrExpenseRequestFormHandler(Long chatId, TelegramWebhookBot bot) {
        Long transactionId = Sessions.getTransactionId(chatId);
        TransactionType type = transactionService.getTransactionTypeById(transactionId);
        System.out.println(type);
        if (type.equals(TransactionType.INCOME)) {
            incomeTypeHandler(chatId, bot);
        } else if (type.equals(TransactionType.EXPENSE)) {
            expenseTypeHandler(chatId, bot);
        } else if (type.equals(TransactionType.TRANSFER)) {
            transferMessageHandler(chatId, type.toString(), bot);
        }
    }

    @SneakyThrows
    private void expenseTypeHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите категорию расходов:");
        sendMessage.setReplyMarkup(markupService.categoryListInlineMarkup(expenseCategoryService.findAll(), userService.findStateByChatId(chatId)));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EXPENSE_TYPE);
    }

    @SneakyThrows
    private void incomeTypeHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите клиента для дохода: ");
        sendMessage.setReplyMarkup(markupService.clientListInlineMarkup(clientService.findAll()));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.INCOME_TYPE);
    }

    @SneakyThrows
    public void otherDateHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Пожалуйста, введите дату в формате yyyy-MM-dd (например, 2024-11-18):");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.OTHER_DATE);
    }

    @SneakyThrows
    public void otherDateStateHandler(Long chatId, String date, Integer messageId, TelegramWebhookBot bot) {
        try {
            LocalDate parsedDate = LocalDate.parse(date, formatter);
            transactionService.updateTransactionDateById(Sessions.getTransactionId(chatId), parsedDate);
            incomeOrExpenseRequestFormHandler(chatId, bot);
        } catch (DateTimeParseException e) {
            SendMessage sendMessage = new SendMessage(chatId.toString(), "Введенная дата имеет неправильный формат. Пожалуйста, введите дату в формате yyyy-MM-dd (например, 2024-11-18).");
            sendMessage.setReplyToMessageId(messageId);
            bot.execute(sendMessage);
        }
    }

    public void expenseTypeStateHandler(Long chatId, String categoryId, TelegramWebhookBot bot) {
        ExpenseCategory category = expenseCategoryService.findById(Integer.valueOf(categoryId));
        transactionService.updateTransactionExpenseCategoryById(Sessions.getTransactionId(chatId), category);
        commentHandler(chatId, bot);
    }

    @SneakyThrows
    private void commentHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы хотите оставить комментарий к транзакции?");
        sendMessage.setReplyMarkup(markupService.commentInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.COMMENT_REQUEST);
    }

    public void commentStateHandler(Long chatId, String comment, TelegramWebhookBot bot) {
        transactionService.updateTransactionCommentById(Sessions.getTransactionId(chatId), comment);
        commentNoStateHandler(chatId, bot);
    }

    @SneakyThrows
    public void commentYesStateHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Напишите комментарий:");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.COMMENT);
    }

    @SneakyThrows
    public void commentNoStateHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Хотите загрузить файл для транзакции?");
        sendMessage.setReplyMarkup(markupService.commentInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.TRANSACTION_FILE);
    }

    @SneakyThrows
    public void transactionFileYesStateHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Отправьте файл");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.REQUEST_TRANSACTION_FILE);
    }

    @SneakyThrows
    public void transactionFileNoStateHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы подтверждаете данные?");
        sendMessage.setReplyMarkup(markupService.informationRequestFormInlineMarkup());
        showTransactionDetails(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.TRANSACTION_CONFIRM_REQUEST);
    }

    @SneakyThrows
    private void showTransactionDetails(Long chatId, TelegramWebhookBot bot) {
        Transaction transaction = transactionService.findById(Sessions.getTransactionId(chatId));
        String text = "";
        if (transaction.getTransactionType().equals(TransactionType.INCOME)) {
            text = "*Транзакция*\n\n" +
                    "*Тип транзакции: *" + showTransactionType(transaction.getTransactionType()) + "\n" +
                    "*Валюта: *" + showTransactionMoneyType(transaction.getMoneyType()) + "\n" +
                    "*Сумма транзакции: *" + transaction.getSumma() + "\n" +
                    "*Дата транзакции: *" + transaction.getTransactionDate() + "\n" +
                    "*Клиент дохода от транзакции: *" + transaction.getClient().getFullName() + "\n" +
                    "*Номер телефона клиента: *" + transaction.getClient().getPhoneNumber() + "\n" +
                    "*Тип услуги клиента: *" + transaction.getClient().getServiceType().getName() + "\n" +
                    "*Статус дохода от транзакции: *" + showTransactionStatus(transaction.getTransactionStatus()) + "\n" +
                    "*Комментарий к транзакции: *" + transaction.getComment() + "\n" +
                    "*Файл транзакции: *" + showTransactionFile(transaction.getFile()) + "\n";
        }
        if (transaction.getTransactionType().equals(TransactionType.EXPENSE)) {
            text = "*Транзакция*\n\n" +
                    "*Тип транзакции: *" + showTransactionType(transaction.getTransactionType()) + "\n" +
                    "*Валюта: *" + showTransactionMoneyType(transaction.getMoneyType()) + "\n" +
                    "*Сумма транзакции: *" + transaction.getSumma() + "\n" +
                    "*Дата транзакции: *" + transaction.getTransactionDate() + "\n" +
                    "*Категория расхода по транзакции: *" + transaction.getExpenseCategory().getName() + "\n" +
                    "*Комментарий к транзакции: *" + showTransactionCommit(transaction.getComment()) + "\n" +
                    "*Файл транзакции: *" + showTransactionFile(transaction.getFile()) + "\n";
        }
        if (transaction.getTransactionType().equals(TransactionType.TRANSFER)) {
            text = "*Транзакция*\n\n" +
                    "*Тип транзакции: *" + showTransactionType(transaction.getTransactionType()) + "\n" +
                    "*Валюта: *" + showTransactionMoneyType(transaction.getMoneyType()) + "\n" +
                    "*Сумма транзакции: *" + transaction.getSumma() + "\n" +
                    "*Дата транзакции: *" + transaction.getTransactionDate() + "\n" +
                    "*Комментарий к транзакции: *" + showTransactionCommit(transaction.getComment()) + "\n" +
                    "*Файл транзакции: *" + showTransactionFile(transaction.getFile()) + "\n";
        }
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        sendMessage.setParseMode("Markdown");
        bot.execute(sendMessage);
    }

    private String showTransactionCommit(String comment) {
        if (comment == null) {
            return "НЕТ❌";
        } else
            return "Есть✅";
    }

    private String showTransactionFile(FileEntity file) {
        if (file == null) {
            return "НЕТ❌";
        } else
            return "Есть✅";
    }

    private String showTransactionStatus(TransactionStatus transactionStatus) {
        if (transactionStatus.equals(TransactionStatus.FINANCIAL_LOAN))
            return "Финзайм";
        if (transactionStatus.equals(TransactionStatus.POST_PAYMENT))
            return "Постоплата";
        if (transactionStatus.equals(TransactionStatus.PRE_PAYMENT))
            return "Предоплата";
        if (transactionStatus.equals(TransactionStatus.SCHEDULED))
            return "Запланировано";
        return " ";
    }

    private String showTransactionMoneyType(MoneyType moneyType) {
        if (moneyType.equals(MoneyType.BANK))
            return "Банк";
        if (moneyType.equals(MoneyType.VISA))
            return "Visa";
        if (moneyType.equals(MoneyType.CARD_AMOUNT1))
            return "Карта суммы 1";
        if (moneyType.equals(MoneyType.CARD_AMOUNT_2))
            return "Карта суммы 2";
        if (moneyType.equals(MoneyType.CARD_AMOUNT_3))
            return "Карта суммы 3";
        if (moneyType.equals(MoneyType.CASH_AMOUNT))
            return "Нал суммы";
        if (moneyType.equals(MoneyType.CASH_CURRENCY))
            return "Нал валюта";
        return " ";
    }

    private String showTransactionType(TransactionType transactionType) {
        if (transactionType.equals(TransactionType.INCOME))
            return "Доход";
        if (transactionType.equals(TransactionType.EXPENSE))
            return "Расход";
        if (transactionType.equals(TransactionType.TRANSFER))
            return "Перемещение";
        return " ";
    }

    public void requestTransactionFileStateHandler(Long chatId, Document document, Integer messageId, TelegramWebhookBot bot) {
        FileEntity fileEntity = processFileMessage(document, bot);
        transactionService.updateTransactionFileById(Sessions.getTransactionId(chatId), fileEntity);
        transactionFileNoStateHandler(chatId, bot);
    }

    @SneakyThrows
    private FileEntity processFileMessage(Document document, TelegramWebhookBot bot) {
        String fileId = document.getFileId();
        GetFile getFileMethod = new GetFile(fileId);
        File file = bot.execute(getFileMethod);
        String filePath = file.getFilePath();
        String fileName = UUID.randomUUID() + "_" + document.getFileName();
        String fileUrl = "https://api.telegram.org/file/bot" + token + "/" + filePath;
        URL url = new URL(fileUrl);
        InputStream input = url.openStream();
        String targetPath = photoFilePath + "/" + fileName;
        Files.copy(input, Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        input.close();
        return fileService.savePhotoFromTelegram(targetPath);
    }

    public void transactionCancellationHandler(Long chatId, TelegramWebhookBot bot) throws IOException {
        addTransactionHandler(chatId, bot);
        transactionService.deleteById(Sessions.getTransactionId(chatId));
        Sessions.removeTransactionId(chatId);
    }

    @SneakyThrows
    public void transactionConfirmHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Транзакция успешно сохранена");
        bot.execute(sendMessage);
        baseMenuForBackHandler(chatId, bot);
        Sessions.removeTransactionId(chatId);
    }

    public void incomeTypeStateHandler(Long chatId, String clientId, TelegramWebhookBot bot) {
        Client client = clientService.findById(Long.valueOf(clientId));
        transactionService.updateTransactionClientById(Sessions.getTransactionId(chatId), client);
        incomeStatusHandler(chatId, bot);
    }

    @SneakyThrows
    private void incomeStatusHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Укажите статус дохода для транзакции");
        sendMessage.setReplyMarkup(markupService.incomeStatusInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.INCOME_TYPE_STATUS);
    }

    public void incomeTypeStatusStateHandler(Long chatId, String status, TelegramWebhookBot bot) {
        transactionService.updateTransactionStatusById(Sessions.getTransactionId(chatId), status);
        commentHandler(chatId, bot);
    }

    @SneakyThrows
    public void clientControlHandler(Long chatId, TelegramWebhookBot bot) {
        trickMessageForClientListHandler(chatId, bot);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Список клиентов");
        sendMessage.setReplyMarkup(markupService.clientListInlineMarkup(clientService.findAll()));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.CLIENT_LIST);
        Sessions.removeClientId(chatId);
    }

    @SneakyThrows
    private void trickMessageForClientListHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете выполнить операции RUD (чтение, обновление, удаление) с выбранным клиентом из списка клиентов или добавить нового клиента.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void addClientHandler(Long chatId, UserState currentState, TelegramWebhookBot bot) {
        if (currentState.equals(UserState.INCOME_TYPE)) {
            transactionService.deleteById(Sessions.getTransactionId(chatId));
            Sessions.removeTransactionId(chatId);
        }
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите имя и фамилию клиента (Ф.И.О):");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.REQUEST_CLIENT_FULL_NAME);
    }

    @SneakyThrows
    public void requestClientFullNameStateHandler(Long chatId, String fullName, TelegramWebhookBot bot) {
        if (clientService.existById(Sessions.getClientId(chatId))) {
            clientService.updateClientFullName(Sessions.getClientId(chatId), fullName);
            showClientDetailsHandler(chatId, Sessions.getClientId(chatId).toString(), bot);
        } else {
            Client client = Client.builder().fullName(fullName).build();
            Long clientId = clientService.save(client).getId();
            Sessions.addClientId(chatId, clientId);
            SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите номер телефона клиента: ");
            bot.execute(sendMessage);
            userService.updateStateByChatId(chatId, UserState.REQUEST_CLIENT_PHONE_NUMBER);
        }
    }

    @SneakyThrows
    public void requestClientPhoneNumberStateHandler(Long chatId, String phoneNumber, TelegramWebhookBot bot) {
        clientService.updateClientPhoneNumber(phoneNumber, Sessions.getClientId(chatId));
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите тип услуги клиента: ");
        sendMessage.setReplyMarkup(markupService.clientServiceListInlineMarkup(serviceTypeService.findAll()));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.REQUEST_CLIENT_SERVICE_CATEGORY);
    }

    @SneakyThrows
    public void setCategoryInClientHandler(Long chatId, String serviceId, TelegramWebhookBot bot) {
        ServiceType service = serviceTypeService.findById(Integer.valueOf(serviceId));
        clientService.updateClientServiceCategory(Sessions.getClientId(chatId), service);
        Sessions.removeClientId(chatId);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Список клиентов");
        sendMessage.setReplyMarkup(markupService.clientListInlineMarkup(clientService.findAll()));
        confirmMessageForClientAddHandler(chatId, bot);
        bot.execute(sendMessage);
    }

    @SneakyThrows
    private void confirmMessageForClientAddHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Клиент успешно добавлен.");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.CLIENT_LIST);
    }


    @SneakyThrows
    public void showClientDetailsHandler(Long chatId, String clientId, TelegramWebhookBot bot) {
        Client client = clientService.findById(Long.valueOf(clientId));
        instructionMessageForClientEdit(chatId, bot);
        String text = "*Клиент*\n\n" +
                "*Ф.И.О: *" + client.getFullName() + "\n" +
                "*Номер телефона: *" + client.getPhoneNumber() + "\n" +
                "*Услуга: *" + client.getServiceType().getName() + "\n";
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.clientSettingInlineMarkupService());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_CLIENT_FORM);
    }

    @SneakyThrows
    private void instructionMessageForClientEdit(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете выбрать поле клиента, которое хотите отредактировать.");
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void editClientHandler(Long chatId, String clientId, TelegramWebhookBot bot) {
        Client client = clientService.findById(Long.valueOf(clientId));
        Sessions.addClientId(chatId, client.getId());
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*" + client.getFullName() + "*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.selectedCategoryInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_CLIENT);
    }

    @SneakyThrows
    public void editClientPhoneNumberHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите номер телефона клиента: ");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_CLIENT_PHONE);
    }

    public void editClientPhoneStateHandler(Long chatId, String phoneNumber, TelegramWebhookBot bot) {
        clientService.updateClientPhoneNumber(phoneNumber, Sessions.getClientId(chatId));
        showClientDetailsHandler(chatId, Sessions.getClientId(chatId).toString(), bot);
    }

    @SneakyThrows
    public void editClientServiceHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите тип услуги клиента: ");
        sendMessage.setReplyMarkup(markupService.clientServiceListInlineMarkup(serviceTypeService.findAll()));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_CLIENT_SERVICE);
    }

    public void editClientServiceStateHandler(Long chatId, String serviceId, TelegramWebhookBot bot) {
        ServiceType service = serviceTypeService.findById(Integer.valueOf(serviceId));
        clientService.updateClientServiceCategory(Sessions.getClientId(chatId), service);
        showClientDetailsHandler(chatId, Sessions.getClientId(chatId).toString(), bot);
    }

    @SneakyThrows
    public void deleteClientHandler(Long chatId, CallbackQuery callbackQuery, TelegramWebhookBot bot) {
        Client client = clientService.findById(Sessions.getClientId(chatId));
        if (client.getTransactionList() != null && !client.getTransactionList().isEmpty()) {
            couldNotClientMessageHandler(callbackQuery, bot);
        } else {
            clientService.deleteById(client.getId());
            SendMessage sendMessage = new SendMessage(chatId.toString(), "Клиент успешно удален.");
            bot.execute(sendMessage);
            clientControlHandler(chatId, bot);
        }
    }

    @SneakyThrows
    private void couldNotClientMessageHandler(CallbackQuery callbackQuery, TelegramWebhookBot bot) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
        answerCallbackQuery.setText("Невозможно удалить этого клиента, так как существуют транзакции, связанные с ним.");
        answerCallbackQuery.setShowAlert(true);
        bot.execute(answerCallbackQuery);
    }

    @SneakyThrows
    public void serviceControlHandler(Long chatId, TelegramWebhookBot bot) {
        trickMessageForServiceListHandler(chatId, bot);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Список услуг");
        sendMessage.setReplyMarkup(markupService.serviceListInlineMarkup(serviceTypeService.findAll()));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.SERVICE_LIST);

    }

    @SneakyThrows
    private void trickMessageForServiceListHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете выполнить операции RUD (чтение, обновление, удаление) с выбранной услугой из списка услуг или добавить новую услугу.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void addServiceHandler(Long chatId, UserState currentState, TelegramWebhookBot bot) {
        if (currentState.equals(UserState.REQUEST_CLIENT_SERVICE_CATEGORY)) {
            clientService.deleteById(Sessions.getClientId(chatId));
            Sessions.removeClientId(chatId);
        }
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите название новой услуги:");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.REQUEST_SERVICE_NAME);
    }

    @SneakyThrows
    public void editServiceHandler(Long chatId, String serviceId, TelegramWebhookBot bot) {
        ServiceType service = serviceTypeService.findById(Integer.valueOf(serviceId));
        Sessions.addServiceId(chatId, service.getId());
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*" + service.getName() + "*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.selectedCategoryInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_SERVICE_FORM);
    }

    @SneakyThrows
    public void requestServiceNameStateHandler(Long chatId, String serviceName, TelegramWebhookBot bot) {
        if (serviceTypeService.existsById(Sessions.getServiceId(chatId))) {
            serviceTypeService.updateServiceName(Sessions.getServiceId(chatId), serviceName);
            showServiceDetailsHandler(chatId, Sessions.getServiceId(chatId).toString(), bot);
        } else {
            ServiceType type = ServiceType.builder().name(serviceName).build();
            Integer id = serviceTypeService.save(type).getId();
            Sessions.addServiceId(chatId, id);
            SendMessage sendMessage = new SendMessage(chatId.toString(), "Услуга успешно сохранена.");
            bot.execute(sendMessage);
            serviceListHandler(chatId, bot);
        }
    }

    @SneakyThrows
    public void serviceListHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Список услуг");
        sendMessage.setReplyMarkup(markupService.serviceListInlineMarkup(serviceTypeService.findAll()));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.SERVICE_LIST);
        Sessions.removeServiceId(chatId);
    }

    @SneakyThrows
    public void deleteServiceHandler(Long chatId, CallbackQuery callbackQuery, TelegramWebhookBot bot) {
        ServiceType service = serviceTypeService.findById(Sessions.getServiceId(chatId));
        if (service.getClientList() != null && !service.getClientList().isEmpty()) {
            couldNotServiceMessageHandler(callbackQuery, bot);
        } else {
            serviceTypeService.deleteById(service.getId());
            SendMessage sendMessage = new SendMessage(chatId.toString(), "Услуга успешно удалена.");
            bot.execute(sendMessage);
            serviceListHandler(chatId, bot);
        }
    }

    @SneakyThrows
    private void couldNotServiceMessageHandler(CallbackQuery callbackQuery, TelegramWebhookBot bot) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
        answerCallbackQuery.setText("Невозможно удалить эту услугу, так как существуют клиенты, связанные с ней.");
        answerCallbackQuery.setShowAlert(true);
        bot.execute(answerCallbackQuery);
    }

    @SneakyThrows
    public void showServiceDetailsHandler(Long chatId, String serviceId, TelegramWebhookBot bot) {
        ServiceType service = serviceTypeService.findById(Integer.valueOf(serviceId));
        instructionMessageForServiceEdit(chatId, bot);
        String text = "*Услуга*\n\n" +
                "*Название: *" + service.getName() + "\n";
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.serviceSettingInlineMarkupService());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_SERVICE);
    }

    @SneakyThrows
    private void instructionMessageForServiceEdit(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете выбрать поле услуги, которое хотите отредактировать.");
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void categoryControlHandler(Long chatId, TelegramWebhookBot bot) {
        trickMessageForCategoryListHandler(chatId, bot);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Список категорий");
        sendMessage.setReplyMarkup(markupService.categoryListInlineMarkup(expenseCategoryService.findAll(), userService.findStateByChatId(chatId)));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.CATEGORY_LIST);
    }

    @SneakyThrows
    private void trickMessageForCategoryListHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете выполнить операции RUD (чтение, обновление, удаление) с выбранной категорией из списка категорий или добавить новую категорию.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void addCategoryHandler(Long chatId, UserState currentState, TelegramWebhookBot bot) {
        if (currentState.equals(UserState.EXPENSE_TYPE)) {
            transactionService.deleteById(Sessions.getTransactionId(chatId));
            Sessions.removeTransactionId(chatId);
        }
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите название новой категории:");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.REQUEST_CATEGORY_NAME);
    }

    @SneakyThrows
    public void requestCategoryNameStateHandler(Long chatId, String categoryName, TelegramWebhookBot bot) {
        if (expenseCategoryService.existsById(Sessions.getCategoryId(chatId))) {
            expenseCategoryService.updateCategoryName(Sessions.getCategoryId(chatId), categoryName);
            showCategoryDetailsHandler(chatId, Sessions.getCategoryId(chatId).toString(), bot);
        } else {
            ExpenseCategory category = ExpenseCategory.builder().name(categoryName).build();
            Integer id = expenseCategoryService.save(category).getId();
            Sessions.addCategoryId(chatId, id);
            SendMessage sendMessage = new SendMessage(chatId.toString(), "Категория успешно сохранена.");
            bot.execute(sendMessage);
            categoryListHandler(chatId, bot);
        }
    }

    @SneakyThrows
    public void categoryListHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Список категорий");
        sendMessage.setReplyMarkup(markupService.categoryListInlineMarkup(expenseCategoryService.findAll(), userService.findStateByChatId(chatId)));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.CATEGORY_LIST);
        Sessions.removeCategoryId(chatId);
    }

    @SneakyThrows
    public void editCategoryHandler(Long chatId, String categoryId, TelegramWebhookBot bot) {
        ExpenseCategory category = expenseCategoryService.findById(Integer.valueOf(categoryId));
        Sessions.addCategoryId(chatId, category.getId());
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*" + category.getName() + "*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.selectedCategoryInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_CATEGORY_FORM);
    }

    @SneakyThrows
    public void showCategoryDetailsHandler(Long chatId, String categoryId, TelegramWebhookBot bot) {
        ExpenseCategory category = expenseCategoryService.findById(Integer.valueOf(categoryId));
        instructionMessageForCategoryEdit(chatId, bot);
        String text = "*Категория*\n\n" +
                "*Название: *" + category.getName() + "\n";
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.serviceSettingInlineMarkupService());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_CATEGORY);
    }

    @SneakyThrows
    private void instructionMessageForCategoryEdit(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете выбрать поле категории, которое хотите отредактировать.");
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void deleteCategoryHandler(Long chatId, CallbackQuery callbackQuery, TelegramWebhookBot bot) {
        ExpenseCategory category = expenseCategoryService.findById(Sessions.getCategoryId(chatId));
        if (category.getTransactionList() != null && !category.getTransactionList().isEmpty()) {
            couldNotCategoryMessageHandler(callbackQuery, bot);
        } else {
            expenseCategoryService.deleteById(category.getId());
            SendMessage sendMessage = new SendMessage(chatId.toString(), "Категория успешно удалена.");
            bot.execute(sendMessage);
            categoryListHandler(chatId, bot);
        }
    }

    @SneakyThrows
    private void couldNotCategoryMessageHandler(CallbackQuery callbackQuery, TelegramWebhookBot bot) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
        answerCallbackQuery.setText("Невозможно удалить эту категорию, так как существуют транзакции, связанные с ней.");
        answerCallbackQuery.setShowAlert(true);
        bot.execute(answerCallbackQuery);
    }

    public void transferMessageHandler(Long chatId, String transactionType, TelegramWebhookBot bot) {
        trickMessageForTypeMessage(chatId, bot);
        userService.updateStateByChatId(chatId, UserState.COMMENT_REQUEST);
    }

    @SneakyThrows
    private void trickMessageForTypeMessage(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*Вы можете оставить комментарий для транзакции.*");
        sendMessage.setParseMode("Markdown");
        bot.execute(sendMessage);
        commentHandler(chatId, bot);
    }
}
