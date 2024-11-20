package uz.result.moneymanagerbot.bot;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
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
import uz.result.moneymanagerbot.service.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
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

    @SneakyThrows
    public void reportControlHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Отчеты:");
        sendMessage.setReplyMarkup(markupService.reportFormReplyMarkup());
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.REPORT_FORM);
    }

    @SneakyThrows
    public void incomeTransactionListForLastMonthHandler(Long chatId, TelegramWebhookBot bot) {
        String monthlyIncomeTransactionList = getMonthlyIncomeTransactionList(transactionService.getIncomeTransactionsForLastMonth());
        SendMessage sendMessage = new SendMessage(chatId.toString(), monthlyIncomeTransactionList);
        sendMessage.setReplyMarkup(markupService.monthlyIncomeReportInlineMarkup());
        sendMessage.setParseMode("Markdown");
        trickMessageForMonthlyIncomeReportHandler(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.MONTHLY_REPORT_LIST);
    }

    private String getMonthlyIncomeTransactionList(List<Transaction> incomeTransactionsForLastMonth) {
        double summa = 0;
        StringBuilder text = new StringBuilder("*Последние ежемесячные отчеты о доходах*\n\n");
        for (Transaction transaction : incomeTransactionsForLastMonth) {
            if (transaction.getTransactionType().equals(TransactionType.INCOME)) {
                text.append("*Тип транзакции: *").append(showTransactionType(transaction.getTransactionType())).append("\n")
                        .append("*Валюта: *").append(showTransactionMoneyType(transaction.getMoneyType())).append("\n")
                        .append("*Сумма транзакции: *").append(transaction.getSumma()).append("\n")
                        .append("*Дата транзакции: *").append(transaction.getTransactionDate()).append("\n")
                        .append("*Клиент дохода от транзакции: *").append(transaction.getClient().getFullName()).append("\n")
                        .append("*Номер телефона клиента: *").append(transaction.getClient().getPhoneNumber()).append("\n")
                        .append("*Тип услуги клиента: *").append(transaction.getClient().getServiceType().getName()).append("\n\n");
                summa += transaction.getSumma();
            }
            if (transaction.getTransactionType().equals(TransactionType.EXPENSE)) {
                text.append("*Тип транзакции: *").append(showTransactionType(transaction.getTransactionType())).append("\n")
                        .append("*Валюта: *").append(showTransactionMoneyType(transaction.getMoneyType())).append("\n")
                        .append("*Сумма транзакции: *").append(transaction.getSumma()).append("\n")
                        .append("*Дата транзакции: *").append(transaction.getTransactionDate()).append("\n")
                        .append("*Категория расхода по транзакции: *").append(transaction.getExpenseCategory().getName()).append("\n\n");
                summa += transaction.getSumma();
            }
            if (transaction.getTransactionType().equals(TransactionType.TRANSFER)) {
                text.append("*Тип транзакции: *").append(showTransactionType(transaction.getTransactionType())).append("\n")
                        .append("*Валюта: *").append(showTransactionMoneyType(transaction.getMoneyType())).append("\n")
                        .append("*Сумма транзакции: *").append(transaction.getSumma()).append("\n")
                        .append("*Дата транзакции: *").append(transaction.getTransactionDate()).append("\n\n");
                summa += transaction.getSumma();
            }
        }
        text.append("Итоговая сумма: ").append(summa);
        return text.toString();
    }


    @SneakyThrows
    private void trickMessageForMonthlyIncomeReportHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Пожалуйста, вы можете ознакомиться с ежемесячными отчетами о доходах!");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    public void installFileIncomeTransactionPdfHandler(Long chatId, TelegramWebhookBot bot) {
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


    private Font loadFont() throws IOException, DocumentException {
        InputStream fontStream = getClass().getResourceAsStream("/fonts/arial.ttf");
        if (fontStream == null) {
            throw new FileNotFoundException("Font file not found in resources: " + "/fonts/arial.ttf");
        }
        BaseFont baseFont = BaseFont.createFont("/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontStream.readAllBytes(), null);
        return new Font(baseFont, (float) 12);
    }

    @SneakyThrows
    public void expenseTransactionListForLastMonthHandler(Long chatId, TelegramWebhookBot bot) {
        String monthlyExpenseTransactionList = getMonthlyIncomeTransactionList(transactionService.getExpenseTransactionsForLastMonth());
        SendMessage sendMessage = new SendMessage(chatId.toString(), monthlyExpenseTransactionList);
        sendMessage.setReplyMarkup(markupService.monthlyIncomeReportInlineMarkup());
        sendMessage.setParseMode("Markdown");
        trickMessageForMonthlyIncomeReportHandler(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.MONTHLY_REPORT_LIST_EXPENSE);
    }

    public void installFileExpenseTransactionPdfHandler(Long chatId, TelegramWebhookBot bot) {
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


    public void additionalReport(Long chatId, TelegramWebhookBot bot) {

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
        String monthlyIncomeTransactionList = getMonthlyIncomeTransactionList(transactionService.findAllIncomeTransactionsWithClientId(Long.valueOf(clientId)));
        SendMessage sendMessage = new SendMessage(chatId.toString(), monthlyIncomeTransactionList);
        sendMessage.setReplyMarkup(markupService.monthlyIncomeReportInlineMarkup());
        sendMessage.setParseMode("Markdown");
        trickMessageForMonthlyIncomeReportHandler(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.FILTER_BY_CLIENT_REPORT_LIST);
        Sessions.addClientId(chatId, Long.valueOf(clientId));
    }

    public void installFileIncomeTransactionByClientPdfHandler(Long chatId, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllIncomeTransactionsWithClientId(Sessions.getClientId(chatId));
        Sessions.removeClientId(chatId);
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
        String monthlyIncomeTransactionList = getMonthlyIncomeTransactionList(transactionService.findAllIncomeTransactionsWithClientService(Integer.valueOf(serviceId)));
        SendMessage sendMessage = new SendMessage(chatId.toString(), monthlyIncomeTransactionList);
        sendMessage.setReplyMarkup(markupService.monthlyIncomeReportInlineMarkup());
        sendMessage.setParseMode("Markdown");
        trickMessageForMonthlyIncomeReportHandler(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.FILTER_BY_SERVICE_REPORT_LIST);
        Sessions.addServiceId(chatId, Integer.valueOf(serviceId));
    }

    public void installFileIncomeTransactionByServicePdfHandler(Long chatId, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllIncomeTransactionsWithClientService(Sessions.getServiceId(chatId));
        Sessions.removeServiceId(chatId);
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
        String monthlyIncomeTransactionList = getMonthlyIncomeTransactionList(transactionService.findAllIncomeTransactionsWithPeriod(period));
        SendMessage sendMessage = new SendMessage(chatId.toString(), monthlyIncomeTransactionList);
        sendMessage.setReplyMarkup(markupService.monthlyIncomeReportInlineMarkup());
        sendMessage.setParseMode("Markdown");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.FILTER_BY_PERIOD_REPORT_LIST);
        Sessions.addPeriod(chatId, period);
    }

    @SneakyThrows
    private void warningMessageForPeriod(Long chatId, Integer messageId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Введённый вами временной период имеет неправильный формат. Укажите период в указанном формате yyyy-MM-dd/yyyy-MM-dd (например, 2024-11-18/2024-12-18).");
        sendMessage.setReplyToMessageId(messageId);
        bot.execute(sendMessage);
    }

    public void installFileIncomeTransactionByPeriodPdfHandler(Long chatId, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllIncomeTransactionsWithPeriod(Sessions.getPeriod(chatId));
        Sessions.removePeriod(chatId);
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
        String monthlyIncomeTransactionList = getMonthlyIncomeTransactionList(transactionService.findAllExpenseTransactionsWithPeriod(period));
        SendMessage sendMessage = new SendMessage(chatId.toString(), monthlyIncomeTransactionList);
        sendMessage.setReplyMarkup(markupService.monthlyIncomeReportInlineMarkup());
        sendMessage.setParseMode("Markdown");
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.FILTER_BY_PERIOD_EXPENSE_REPORT_LIST);
        Sessions.addPeriod(chatId, period);
    }

    public void installFileExpenseTransactionByPeriodPdfHandler(Long chatId, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllExpenseTransactionsWithPeriod(Sessions.getPeriod(chatId));
        Sessions.removePeriod(chatId);
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
        String monthlyIncomeTransactionList = getMonthlyIncomeTransactionList(transactionService.findAllExpenseTransactionsWithClientCategory(Integer.valueOf(categoryId)));
        SendMessage sendMessage = new SendMessage(chatId.toString(), monthlyIncomeTransactionList);
        sendMessage.setReplyMarkup(markupService.monthlyIncomeReportInlineMarkup());
        sendMessage.setParseMode("Markdown");
        trickMessageForMonthlyExpanseReportHandler(chatId, bot);
        bot.execute(sendMessage);
        userService.updateStateByChatId(chatId, UserState.FILTER_BY_CATEGORY_REPORT_LIST);
        Sessions.addCategoryId(chatId, Integer.valueOf(categoryId));
    }

    @SneakyThrows
    private void trickMessageForMonthlyExpanseReportHandler(Long chatId, TelegramWebhookBot bot) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Вы выбрали фильтрацию месячных расходов по категории расходов.");
        sendMessage.setReplyMarkup(markupService.removeReplyMarkup());
        bot.execute(sendMessage);
    }

    public void installFileExpenseTransactionByCategoryPdfHandler(Long chatId, TelegramWebhookBot bot) {
        List<Transaction> transactions = transactionService.findAllExpenseTransactionsWithClientCategory(Sessions.getCategoryId(chatId));
        Sessions.removeCategoryId(chatId);

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


}
