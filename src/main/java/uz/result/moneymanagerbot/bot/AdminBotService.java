package uz.result.moneymanagerbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.result.moneymanagerbot.model.*;
import uz.result.moneymanagerbot.model.User;
import uz.result.moneymanagerbot.service.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
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

    private final NotificationService notificationService;

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
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
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
        userService.signIn(chatId);
        baseMenuHandler(chatId, confirmMessageId, bot);
    }

//    @SneakyThrows
//    private void baseMenuHandler(Long chatId, Integer messageId, TelegramWebhookBot bot) {
//        SendMessage sendMessage = new SendMessage(chatId.toString(), "*\uD83D\uDEE1 Вы можете полностью воспользоваться возможностями администратора*");
//        sendMessage.setParseMode("Markdown");
//        sendMessage.setReplyToMessageId(messageId);
//        sendMessage.setReplyMarkup(markupService.baseMenuReplyMarkupService());
//        bot.execute(sendMessage);
//        userService.updateStateByChatId(chatId, UserState.BASE_MENU);
//    }

    @SneakyThrows
    public void menuHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*\uD83D\uDEE1 Вы можете полностью использовать возможности администратора*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.baseMenuReplyMarkupService());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.BASE_MENU);
    }

    @SneakyThrows
    private void baseMenuHandler(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*\uD83D\uDEE1 Вы можете полностью воспользоваться возможностями администратора*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyToMessageId(messageId);
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
    public void fromMenuToBaseMenuHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "*\uD83D\uDEE1 Вы можете полностью использовать возможности администратора*");
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.transactionTypeReplyMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADD_TRANSACTION);
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
        String text = "⚠️ *Введите сумму в указанном формате, то есть только из цифр:* ";
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
        return Objects.requireNonNullElse(comment, "НЕТ❌");
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
//        baseMenuForBackHandler(chatId, bot);
        fromMenuToBaseMenuHandler(chatId, bot);
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

    @SneakyThrows
    public void reportControlHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Отчеты:");
        sendMessage.setReplyMarkup(markupService.reportFormReplyMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.REPORT_FORM);
    }

    @SneakyThrows
    public void incomeTransactionListForLastMonthHandler(Long chatId, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.getIncomeTransactionsForLastMonth();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Отчет о доходных транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Клиент дохода");
            headerRow.createCell(5).setCellValue("Номер телефона клиента");
            headerRow.createCell(6).setCellValue("Тип услуги клиента");

            int rowIdx = 1;
            double summa = 0;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());
                row.createCell(4).setCellValue(transaction.getClient().getFullName());
                row.createCell(5).setCellValue(transaction.getClient().getPhoneNumber());
                row.createCell(6).setCellValue(transaction.getClient().getServiceType().getName());
                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_доходных_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);

            reportControlHandler(chatId, bot);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void expenseTransactionListForLastMonthHandler(Long chatId, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.getExpenseTransactionsForLastMonth();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Отчет о транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Категория расхода");

            int rowIdx = 1;
            double summa = 0;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());
                row.createCell(4).setCellValue(transaction.getExpenseCategory().getName());
                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);

            reportControlHandler(chatId, bot);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void saldoViewHandler(Long chatId, TelegramWebhookBot bot) {
        List<Transaction> transactionList = transactionService.findAll();
        double income = 0;
        double expense = 0;

        for (Transaction transaction : transactionList) {
            if (transaction.getTransactionType() != null && transaction.getSumma() != null) {
                if (transaction.getTransactionType().equals(TransactionType.INCOME)) {
                    income += transaction.getSumma();
                }
                if (transaction.getTransactionType().equals(TransactionType.EXPENSE)) {
                    expense += transaction.getSumma();
                }
            }
        }

        String saldoMessage = "Сальдо: " + (income - expense);
        SendMessage sendMessage = new SendMessage(chatId.toString(), saldoMessage);
        bot.execute(sendMessage);
    }


    @SneakyThrows
    public void additionalReport(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "По какому фильтру вы хотите увидеть отчет?");
        sendMessage.setReplyMarkup(markupService.additionalFilterReportReplyMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_REPORT);
    }

    @SneakyThrows
    public void incomeFilterForLastMonthHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы хотите воспользоваться дополнительной возможностью фильтрации? ");
        sendMessage.setReplyMarkup(markupService.additionalReportReplyMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_FILTER_INCOME);
    }

    @SneakyThrows
    public void incomeTransactionListFilterByClient(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите клиента.");
        sendMessage.setReplyMarkup(markupService.clientListForFilterInlineMarkup(clientService.findAll()));
        trickMessageForFilterByClientTransaction(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_FILTER_INCOME_CLIENT);
    }

    @SneakyThrows
    private void trickMessageForFilterByClientTransaction(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы выбрали фильтрацию месячных доходов по клиенту.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void incomeTransactionListFilterByClientHandler(Long chatId, String clientId, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllIncomeTransactionsWithClientId(Long.valueOf(clientId));
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Отчет о доходных транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Клиент дохода");
            headerRow.createCell(5).setCellValue("Номер телефона клиента");
            headerRow.createCell(6).setCellValue("Тип услуги клиента");

            int rowIdx = 1;
            double summa = 0;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());
                row.createCell(4).setCellValue(transaction.getClient().getFullName());
                row.createCell(5).setCellValue(transaction.getClient().getPhoneNumber());
                row.createCell(6).setCellValue(transaction.getClient().getServiceType().getName());
                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_доходных_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);

            reportControlHandler(chatId, bot);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }


    @SneakyThrows
    public void incomeTransactionListFilterByService(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите услугу.");
        sendMessage.setReplyMarkup(markupService.serviceListForFilterInlineMarkup(serviceTypeService.findAll()));
        trickMessageForFilterByServiceTransaction(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_FILTER_INCOME_SERVICE);
    }

    @SneakyThrows
    private void trickMessageForFilterByServiceTransaction(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы выбрали фильтрацию месячных доходов по услуге.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void incomeTransactionListFilterByServiceHandler(Long chatId, String serviceId, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllIncomeTransactionsWithClientService(Integer.valueOf(serviceId));
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Отчет о доходных транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Клиент дохода");
            headerRow.createCell(5).setCellValue("Номер телефона клиента");
            headerRow.createCell(6).setCellValue("Тип услуги клиента");

            int rowIdx = 1;
            double summa = 0;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());
                row.createCell(4).setCellValue(transaction.getClient().getFullName());
                row.createCell(5).setCellValue(transaction.getClient().getPhoneNumber());
                row.createCell(6).setCellValue(transaction.getClient().getServiceType().getName());
                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_доходных_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);

            reportControlHandler(chatId, bot);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void incomeTransactionListFilterByPeriod(Long chatId, TelegramWebhookBot bot) {
        trickMessageForFilterByPeriodTransaction(chatId, bot);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Укажите период в указанном формате yyyy-MM-dd/yyyy-MM-dd (например, 2024-11-18/2024-12-18).");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_FILTER_INCOME_SERVICE_DATE);
    }

    @SneakyThrows
    private void trickMessageForFilterByPeriodTransaction(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы выбрали фильтрацию месячных доходов по периоду.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void incomeTransactionListFilterByPeriodShow(Long chatId, String period, Integer messageId, TelegramWebhookBot bot) {
        if (!validationService.isValidDateRange(period)) {
            warningMessageForPeriod(chatId, messageId, bot);
            return;
        }
        List<Transaction> transactions = transactionService.findAllIncomeTransactionsWithPeriod(period);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Отчет о доходных транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Клиент дохода");
            headerRow.createCell(5).setCellValue("Номер телефона клиента");
            headerRow.createCell(6).setCellValue("Тип услуги клиента");

            int rowIdx = 1;
            double summa = 0;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());
                row.createCell(4).setCellValue(transaction.getClient().getFullName());
                row.createCell(5).setCellValue(transaction.getClient().getPhoneNumber());
                row.createCell(6).setCellValue(transaction.getClient().getServiceType().getName());
                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_доходных_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);

            reportControlHandler(chatId, bot);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private void warningMessageForPeriod(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введённый вами временной период имеет неправильный формат. Укажите период в указанном формате yyyy-MM-dd/yyyy-MM-dd (например, 2024-11-18/2024-12-18).");
        sendMessage.setReplyToMessageId(messageId);
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void expenseFilterForLastMonthHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы хотите воспользоваться дополнительной возможностью фильтрации? ");
        sendMessage.setReplyMarkup(markupService.additionalExpenseReportReplyMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_FILTER_EXPENSE);
    }

    @SneakyThrows
    public void expenseTransactionListFilterByPeriod(Long chatId, TelegramWebhookBot bot) {
        trickMessageForFilterByPeriodExpenseTransaction(chatId, bot);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Укажите период в указанном формате yyyy-MM-dd/yyyy-MM-dd (например, 2024-11-18/2024-12-18).");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_FILTER_EXPENSE_DATE);
    }

    @SneakyThrows
    private void trickMessageForFilterByPeriodExpenseTransaction(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы выбрали фильтрацию ежемесячных расходов по периоду.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void expenseTransactionListFilterByPeriodShow(Long chatId, String period, Integer messageId, TelegramWebhookBot bot) {
        if (!validationService.isValidDateRange(period)) {
            warningMessageForPeriod(chatId, messageId, bot);
            return;
        }
        List<Transaction> transactions = transactionService.findAllExpenseTransactionsWithPeriod(period);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Отчет о транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Категория расхода");

            int rowIdx = 1;
            double summa = 0;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());
                row.createCell(4).setCellValue(transaction.getExpenseCategory().getName());
                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);

            reportControlHandler(chatId, bot);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void expenseTransactionListFilterByServiceOfCategory(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите категорию расходов.");
        sendMessage.setReplyMarkup(markupService.serviceOfCategoryListForFilterInlineMarkup(expenseCategoryService.findAll()));
        trickMessageForFilterByCategoryTransaction(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_FILTER_INCOME_CATEGORY);
    }

    @SneakyThrows
    private void trickMessageForFilterByCategoryTransaction(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы выбрали фильтрацию ежемесячных расходов по категории расходов.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void expenseTransactionListFilterByCategoryHandler(Long chatId, String categoryId, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllExpenseTransactionsWithClientCategory(Integer.valueOf(categoryId));
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Отчет о транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Категория расхода");

            int rowIdx = 1;
            double summa = 0;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());
                row.createCell(4).setCellValue(transaction.getExpenseCategory().getName());
                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);

            reportControlHandler(chatId, bot);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void additionalReportByPeriod(Long chatId, TelegramWebhookBot bot) {
        trickMessageForFilterByPeriodAdditionalReport(chatId, bot);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Укажите период в указанном формате yyyy-MM-dd/yyyy-MM-dd (например, 2024-11-18/2024-12-18).");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_REPORT_DATE);
    }

    @SneakyThrows
    private void trickMessageForFilterByPeriodAdditionalReport(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете посмотреть отчеты по периодам.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void additionalReportDateStateHandler(Long chatId, String period, Integer messageId, TelegramWebhookBot bot) {
        if (!validationService.isValidDateRange(period)) {
            warningMessageForPeriod(chatId, messageId, bot);
            return;
        }
        List<Transaction> transactions = transactionService.findAllTransactionsWithPeriod(period);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Отчет о транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Категория расхода / Клиент дохода");
            headerRow.createCell(5).setCellValue("Номер телефона клиента");
            headerRow.createCell(6).setCellValue("Тип услуги клиента");

            int rowIdx = 1;
            double summa = 0;

            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());

                if (transaction.getTransactionType().equals(TransactionType.INCOME)) {
                    row.createCell(4).setCellValue(transaction.getClient().getFullName());
                    row.createCell(5).setCellValue(transaction.getClient().getPhoneNumber());
                    row.createCell(6).setCellValue(transaction.getClient().getServiceType().getName());
                } else if (transaction.getTransactionType().equals(TransactionType.EXPENSE)) {
                    row.createCell(4).setCellValue(transaction.getExpenseCategory().getName());
                }

                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);

            reportControlHandler(chatId, bot);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void additionalReportByMoneyType(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите валюту");
        sendMessage.setReplyMarkup(markupService.moneyTypeListInlineMarkupKeyBoard());
        trickMessageForAdditionalReportByMoneyType(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_REPORT_BY_MONEY_TYPE);
    }

    @SneakyThrows
    private void trickMessageForAdditionalReportByMoneyType(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете посмотреть дополнительные отчеты по типу валюты");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void showAdditionalReportByMoneyType(Long chatId, String moneyType, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllTransactionsByMoneyType(moneyType);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Отчет о транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Категория расхода / Клиент дохода");
            headerRow.createCell(5).setCellValue("Номер телефона клиента");
            headerRow.createCell(6).setCellValue("Тип услуги клиента");

            int rowIdx = 1;
            double summa = 0;

            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());

                if (transaction.getTransactionType().equals(TransactionType.INCOME)) {
                    row.createCell(4).setCellValue(transaction.getClient().getFullName());
                    row.createCell(5).setCellValue(transaction.getClient().getPhoneNumber());
                    row.createCell(6).setCellValue(transaction.getClient().getServiceType().getName());
                } else if (transaction.getTransactionType().equals(TransactionType.EXPENSE)) {
                    row.createCell(4).setCellValue(transaction.getExpenseCategory().getName());
                }

                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);

            reportControlHandler(chatId, bot);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void additionalReportByTransactionType(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете посмотреть дополнительные отчеты по типу транзакции");
        sendMessage.setReplyMarkup(markupService.transactionTypeReplyMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.ADDITIONAL_REPORT_BY_TRANSACTION_TYPE);
    }

    @SneakyThrows
    public void additionalReportListByTransactionType(Long chatId, String transactionType, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllTransactionsByTransactionType(Objects.requireNonNull(getTransactionType(transactionType)).toString());

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Отчет о транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Категория расхода / Клиент дохода");
            headerRow.createCell(5).setCellValue("Номер телефона клиента");
            headerRow.createCell(6).setCellValue("Тип услуги клиента");

            int rowIdx = 1;
            double summa = 0;

            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());

                if (transaction.getTransactionType().equals(TransactionType.INCOME)) {
                    row.createCell(4).setCellValue(transaction.getClient().getFullName());
                    row.createCell(5).setCellValue(transaction.getClient().getPhoneNumber());
                    row.createCell(6).setCellValue(transaction.getClient().getServiceType().getName());
                } else if (transaction.getTransactionType().equals(TransactionType.EXPENSE)) {
                    row.createCell(4).setCellValue(transaction.getExpenseCategory().getName());
                }

                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);

            reportControlHandler(chatId, bot);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void transactionListByType(Long chatId, String transactionType, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllTransactionsByTransactionType(Objects.requireNonNull(getTransactionType(transactionType)).toString());

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Отчет о транзакциях");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Тип транзакции");
            headerRow.createCell(1).setCellValue("Валюта");
            headerRow.createCell(2).setCellValue("Сумма транзакции");
            headerRow.createCell(3).setCellValue("Дата транзакции");
            headerRow.createCell(4).setCellValue("Категория расхода / Клиент дохода");
            headerRow.createCell(5).setCellValue("Номер телефона клиента");
            headerRow.createCell(6).setCellValue("Тип услуги клиента");

            int rowIdx = 1;
            double summa = 0;

            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(showTransactionType(transaction.getTransactionType()));
                row.createCell(1).setCellValue(showTransactionMoneyType(transaction.getMoneyType()));
                row.createCell(2).setCellValue(transaction.getSumma());
                row.createCell(3).setCellValue(transaction.getTransactionDate().toString());

                if (transaction.getTransactionType().equals(TransactionType.INCOME)) {
                    row.createCell(4).setCellValue(transaction.getClient().getFullName());
                    row.createCell(5).setCellValue(transaction.getClient().getPhoneNumber());
                    row.createCell(6).setCellValue(transaction.getClient().getServiceType().getName());
                } else if (transaction.getTransactionType().equals(TransactionType.EXPENSE)) {
                    row.createCell(4).setCellValue(transaction.getExpenseCategory().getName());
                }

                summa += transaction.getSumma();
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Итоговая сумма:");

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelCell.setCellStyle(style);

            Cell totalSumCell = totalRow.createCell(2);
            totalSumCell.setCellValue(summa);
            totalSumCell.setCellStyle(style);

            workbook.write(outputStream);

            InputFile inputFile = new InputFile(new ByteArrayInputStream(outputStream.toByteArray()), "Отчет_о_транзакциях.xlsx");
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);
            bot.execute(sendDocument);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void viewBalanceHandler(Long chatId, TelegramWebhookBot bot) {

    }

    @SneakyThrows
    public void settingsHandler(Long chatId, TelegramWebhookBot bot) {
        UserRole role = userService.findRoleByChatId(chatId);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Настройки");
        sendMessage.setReplyMarkup(markupService.settingsFormReplyMarkup(role));
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.SETTING);
    }

    @SneakyThrows
    public void currentAccessRight(Long chatId, TelegramWebhookBot bot) {
        UserRole role = userService.findRoleByChatId(chatId);
        String text = "";
        if (role.equals(UserRole.SUPPER_ADMIN)) {
            text = "В вашей учётной записи имеется роль SUPER_ADMIN. Ваши права доступа к боту не ограничены.";
        }
        if (role.equals(UserRole.ADMIN)) {
            text = """
                    В вашей учётной записи имеется роль ADMIN. У вас есть право использовать все функции бота, \s
                    за исключением управления пользователями бота (это право принадлежит только SUPER_ADMIN).
                    """;
        }
        if (role.equals(UserRole.OBSERVER)) {
            text = "В вашей учётной записи имеется роль OBSERVER. Вы можете просматривать данные в боте, но не можете их изменять.";
        }
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void editPasswordHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите текущий пароль.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.CURRENT_PASSWORD);
    }

    @SneakyThrows
    public void requestCurrentPassword(Long chatId, String password, Integer messageId, TelegramWebhookBot bot) {
        if (!userService.findByChatId(chatId).getPassword().equals(password)) {
            warningMessageForWrongPassword(chatId, messageId, bot);
            return;
        }
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите новый пароль: ");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NEW_PASSWORD);
    }

    @SneakyThrows
    public void requestNewPassword(Long chatId, String password, Integer messageId, TelegramWebhookBot bot) {
        userService.updateUserPasswordById(password, chatId);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Пароль успешно изменён.✅");
        sendMessage.setReplyToMessageId(messageId);
        bot.execute(sendMessage);
        settingsHandler(chatId, bot);
    }

    @SneakyThrows
    public void controlUsersHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Список пользователей.");
        sendMessage.setReplyMarkup(markupService.userListInlineMarkup(userService.findAllExpectSuperAdmin()));
        trickMessageForUserList(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.USER_LIST);
        Sessions.removeUser(chatId);
    }

    @SneakyThrows
    private void trickMessageForUserList(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете добавить нового пользователя или отредактировать существующих.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void addUserHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите имя нового пользователя: ");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NEW_USER_NAME);
    }

    @SneakyThrows
    public void newUserNameStateHandler(Long chatId, String name, TelegramWebhookBot bot) {
        User user = Sessions.getUser(chatId);
        user.setName(name);
        Sessions.updateUser(chatId, user);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите chatId нового пользователя: ");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NEW_USER_CHAT_ID);
    }

    @SneakyThrows
    public void newUserChatIdStateHandler(Long chatId, String userChatId, Integer messageId, TelegramWebhookBot bot) {
        if (!validationService.isValidLong(userChatId)) {
            warningMessageForWrongChatId(chatId, messageId, bot);
            return;
        }
        User user = Sessions.getUser(chatId);
        user.setChatId(Long.valueOf(userChatId));
        Sessions.updateUser(chatId, user);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите роль пользователя: ");
        sendMessage.setReplyMarkup(markupService.roleInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.USER_ROLE);
    }

    @SneakyThrows
    private void warningMessageForWrongChatId(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Пожалуйста, введите правильный chat ID (например: 1234567890).");
        sendMessage.setReplyToMessageId(messageId);
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void userRoleStateHandler(Long chatId, String role, TelegramWebhookBot bot) {
        User user = Sessions.getUser(chatId);
        user.setRole(UserRole.valueOf(role));
        userService.save(user);
        Sessions.removeUser(chatId);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Пользователь успешно сохранён.✅");
        bot.execute(sendMessage);
        controlUsersHandler(chatId, bot);
    }

    @SneakyThrows
    public void showUserNameFormHandler(Long chatId, String userId, TelegramWebhookBot bot) {
        User user = userService.findById(Integer.valueOf(userId));
        SendMessage sendMessage = new SendMessage(chatId.toString(), user.getName());
        sendMessage.setReplyMarkup(markupService.selectedCategoryInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.USER_SHOW);
        Sessions.updateUser(chatId, user);
    }

    @SneakyThrows
    public void showUserFormHandler(Long chatId, TelegramWebhookBot bot) {
        User user = userService.findById(Sessions.getUser(chatId).getId());
        String text = "*User*\n\n" +
                "*Name: *" + user.getName() + "\n" +
                "*Role: *" + user.getRole() + "\n" +
                "*ChatId: *" + user.getChatId() + "\n\n";
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.userEditFormInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_USER);
    }

    @SneakyThrows
    public void deleteUserHandler(Long chatId, TelegramWebhookBot bot) {
        userService.delete(Sessions.getUser(chatId).getId());
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Пользователь успешно удалён.");
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void requestUserEditNameHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите новое имя пользователя:: ");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.USER_EDIT_NAME);
    }

    public void requestUserEditNameStateHandler(Long chatId, String text, TelegramWebhookBot bot) {
        userService.updateUserNameById(text, Sessions.getUser(chatId).getId());
        showUserFormHandler(chatId, bot);
    }

    @SneakyThrows
    public void requestUserEditRoleHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите роль пользователя:");
        sendMessage.setReplyMarkup(markupService.roleInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.USER_ROLE_EDIT);
    }

    public void updateUserRoleHandler(Long chatId, String data, TelegramWebhookBot bot) {
        userService.updateUserRoleById(data, Sessions.getUser(chatId).getId());
        showUserFormHandler(chatId, bot);
    }

    @SneakyThrows
    public void requestUserEditChatIdHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите chatId пользователя:");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.USER_CHAT_ID_EDIT);
    }

    public void userChatIdEditHandler(Long chatId, String chatIdByUser, Integer messageId, TelegramWebhookBot bot) {
        if (!validationService.isValidLong(chatIdByUser)) {
            warningMessageForWrongChatId(chatId, messageId, bot);
            return;
        }
        userService.updateUserChatIdById(Long.valueOf(chatIdByUser), Sessions.getUser(chatId).getId());
        showUserFormHandler(chatId, bot);
    }

    @SneakyThrows
    public void balanceViewHandler(Long chatId, TelegramWebhookBot bot) {
        List<Transaction> transactionList = transactionService.findAll();
        double income = 0;
        double expense = 0;

        for (Transaction transaction : transactionList) {
            if (transaction.getTransactionType() != null && transaction.getSumma() != null) {
                if (transaction.getTransactionType().equals(TransactionType.INCOME)) {
                    income += transaction.getSumma();
                }
                if (transaction.getTransactionType().equals(TransactionType.EXPENSE)) {
                    expense += transaction.getSumma();
                }
            }
        }

        String text = "*Общая сумма дохода: * " + income + "\n\n" +
                "*Общая сумма расходов: *" + expense + "\n\n";
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        sendMessage.setParseMode("Markdown");
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void notificationHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Список уведомлений");
        sendMessage.setReplyMarkup(markupService.notificationListInline(notificationService.findAll()));
        trickMessageForNotificationList(chatId, bot);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_LIST);
        bot.execute(sendMessage);
        Sessions.removeNotification(chatId);
    }

    @SneakyThrows
    private void trickMessageForNotificationList(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы можете увидеть список уведомлений" +
                "и вы можете выполнять различные действия над существующими уведомлениями");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void addNotificationHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите дату уведомления в формате гггг-ММ-дд ЧЧ:мм (пример: 2024-11-22 15:30).");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_DATE);
    }

    @SneakyThrows
    public void notificationDateStateHandler(Long chatId, String time, Integer messageId, TelegramWebhookBot bot) {
        if (!validationService.isValidDateTime(time)) {
            warningMessageForWrongFormatNotificationTime(chatId, messageId, bot);
            return;
        }
        LocalDateTime dateTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Notification notification = Sessions.getNotification(chatId);
        notification.setTime(dateTime);
        Sessions.addNotification(chatId, notification);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите текст сообщения: ");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_MESSAGE);
    }

    @SneakyThrows
    private void warningMessageForWrongFormatNotificationTime(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введенный вами формат времени неверен. Введите правильный формат гггг-ММ-дд ЧЧ:мм (пример: 2024-11-22 15:30).");
        sendMessage.setReplyToMessageId(messageId);
        bot.execute(sendMessage);
    }

    @SneakyThrows
    public void notificationMessageStateHandle(Long chatId, String text, TelegramWebhookBot bot) {
        Notification notification = Sessions.getNotification(chatId);
        notification.setMessage(text);
        Sessions.addNotification(chatId, notification);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите сумму транзакции (Только числа): ");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_SUMMA);
    }

    @SneakyThrows
    public void notificationSummaStateHandler(Long chatId, String summa, Integer messageId, TelegramWebhookBot bot) {
        if (!validationService.isValidDouble(summa)) {
            warningMessageForPriceHandler(chatId, messageId, bot);
            return;
        }
        Notification notification = Sessions.getNotification(chatId);
        notification.setSumma(Double.valueOf(summa));
        Sessions.addNotification(chatId, notification);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите тип транзакции");
        sendMessage.setReplyMarkup(markupService.notificationTypeInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_TYPE);
    }

    @SneakyThrows
    public void notificationTypeStateHandler(Long chatId, String type, TelegramWebhookBot bot) {
        Notification notification = Sessions.getNotification(chatId);
        notification.setType(TransactionType.valueOf(type));
        Sessions.addNotification(chatId, notification);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите тип уведомления: ");
        sendMessage.setReplyMarkup(markupService.notificationSelectTypeInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_REPEAT_TIME);
    }

    @SneakyThrows
    public void notificationRepeatTime(Long chatId, String data, TelegramWebhookBot bot) {
        Notification notification = Sessions.getNotification(chatId);
        notification.setRepeatInterval(RepeatPeriod.valueOf(data));
        notificationService.save(notification);
        Sessions.removeNotification(chatId);
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Уведомление успешно сохранено✅");
        bot.execute(sendMessage);
        notificationHandler(chatId, bot);
    }

    @SneakyThrows
    public void notificationControlHandler(Long chatId, String id, TelegramWebhookBot bot) {
        Notification notification = notificationService.findById(Integer.valueOf(id));
        SendMessage sendMessage = new SendMessage(chatId.toString(), notification.getTime().format(formatter));
        sendMessage.setReplyMarkup(markupService.selectedCategoryInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_CONTROL);
        Sessions.addNotification(chatId, notification);
    }

    @SneakyThrows
    public void deleteNotificationHandler(Long chatId, TelegramWebhookBot bot) {
        notificationService.deleteById(Sessions.getNotification(chatId).getId());
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Уведомление успешно удалено✅");
        bot.execute(sendMessage);
        notificationHandler(chatId, bot);
    }

    @SneakyThrows
    public void notificationEditFormHandler(Long chatId, TelegramWebhookBot bot) {
        Integer id = Sessions.getNotification(chatId).getId();
        Notification notification = notificationService.findById(id);
        String text = "*Уведомление*\n\n" +
                "*Дата: *" + notification.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                "*Текст: *" + notification.getMessage() + "\n" +
                "*Сумма транзакции: *" + notification.getSumma() + "\n" +
                "*Тип транзакции: *" + showTransactionType(notification.getType()) + "\n" +
                "*Тип уведомления: *" + showNotificationType(notification.getRepeatInterval()) + "\n";
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(markupService.notificationEditFormInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_NOTIFICATION);
    }

    private String showNotificationType(RepeatPeriod repeatInterval) {
        if (repeatInterval.equals(RepeatPeriod.MONTHLY))
            return "Раз в месяц";
        if (repeatInterval.equals(RepeatPeriod.ONCE))
            return "Одноразово";
        if (repeatInterval.equals(RepeatPeriod.WEEKLY))
            return "Раз в неделю";
        if (repeatInterval.equals(RepeatPeriod.QUARTERLY))
            return "Раз в три месяца";
        return "";
    }

    @SneakyThrows
    public void editNotifTimeHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите дату уведомления в формате гггг-ММ-дд ЧЧ:мм (пример: 2024-11-22 15:30).");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_EDIT_DATE);
    }

    public void notifEditStateHandler(Long chatId, String time, Integer messageId, TelegramWebhookBot bot) {
        if (!validationService.isValidDateTime(time)) {
            warningMessageForWrongFormatNotificationTime(chatId, messageId, bot);
            return;
        }
        LocalDateTime dateTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Notification notification = Sessions.getNotification(chatId);
        notificationService.updateNotificationTimeById(dateTime, notification.getId());
        notificationEditFormHandler(chatId, bot);
    }

    @SneakyThrows
    public void editNotifTextHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите текст уведомления: ");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.EDIT_NOTIFICATION_TEXT);
    }

    public void notifEditTextStateHandler(Long chatId, String text, TelegramWebhookBot bot) {
        Integer id = Sessions.getNotification(chatId).getId();
        notificationService.updateNotificationTextById(id, text);
        notificationEditFormHandler(chatId, bot);
    }

    @SneakyThrows
    public void editNotifSummaHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введите сумму транзакции (Только числа): ");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_EDIT_SUMMA);
    }

    public void notifEditSummaStateHandler(Long chatId, String summa, Integer messageId, TelegramWebhookBot bot) {
        if (!validationService.isValidDouble(summa)) {
            warningMessageForPriceHandler(chatId, messageId, bot);
            return;
        }
        Integer id = Sessions.getNotification(chatId).getId();
        notificationService.updateNotificationSummaById(id, Double.valueOf(summa));
        notificationEditFormHandler(chatId, bot);
    }

    @SneakyThrows
    public void editNotifTranEditTypeHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите тип транзакции");
        sendMessage.setReplyMarkup(markupService.notificationTypeInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_TYPE_EDIT);
    }

    public void notificationTypeEditStateHandler(Long chatId, String data, TelegramWebhookBot bot) {
        Integer id = Sessions.getNotification(chatId).getId();
        notificationService.updateNotificationTransactionTypeById(data, id);
        notificationEditFormHandler(chatId, bot);
    }

    @SneakyThrows
    public void editNotificationTypeHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите тип уведомления: ");
        sendMessage.setReplyMarkup(markupService.notificationSelectTypeInlineMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.NOTIFICATION_REPEAT_TIME_EDIT);
    }

    public void notificationTimeRepeatEditHandler(Long chatId, String data, TelegramWebhookBot bot) {
        Integer id = Sessions.getNotification(chatId).getId();
        notificationService.updateNotificationTimeRepeatById(data,id);
        notificationEditFormHandler(chatId, bot);
    }
}
