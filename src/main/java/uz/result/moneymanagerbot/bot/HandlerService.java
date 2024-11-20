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
                    Sessions.clearSessions();
                    currentState = userService.findStateByChatId(chatId);
                }
            }
            switch (currentState) {
                case DEFAULT -> adminBotService.defaultStateHandler(chatId, text, message, bot);
                case START -> adminBotService.startStateHandler(chatId, bot);
                case PASSWORD -> adminBotService.passwordStateHandler(chatId, text, message.getMessageId(), bot);
                case TRANSACTION_SUMMA ->
                        adminBotService.transactionSummaStateHandler(chatId, text, message.getMessageId(), bot);
                case OTHER_DATE -> adminBotService.otherDateStateHandler(chatId, text, message.getMessageId(), bot);
                case COMMENT -> adminBotService.commentStateHandler(chatId, text, bot);
                case TRANSACTION_DATE -> adminBotService.transactionDateMessageHandler(chatId, bot);
                case REQUEST_CLIENT_FULL_NAME -> adminBotService.requestClientFullNameStateHandler(chatId, text, bot);
                case REQUEST_CLIENT_PHONE_NUMBER ->
                        adminBotService.requestClientPhoneNumberStateHandler(chatId, text, bot);
                case EDIT_CLIENT_PHONE -> adminBotService.editClientPhoneStateHandler(chatId, text, bot);
                case REQUEST_SERVICE_NAME -> adminBotService.requestServiceNameStateHandler(chatId, text, bot);
                case REQUEST_CATEGORY_NAME -> adminBotService.requestCategoryNameStateHandler(chatId, text, bot);
                case ADDITIONAL_FILTER_INCOME_SERVICE_DATE ->
                        adminBotService.incomeTransactionListFilterByPeriodShow(chatId, text, message.getMessageId(), bot);
                case ADDITIONAL_FILTER_EXPENSE_DATE ->
                        adminBotService.expenseTransactionListFilterByPeriodShow(chatId, text, message.getMessageId(), bot);
                case ADDITIONAL_REPORT_DATE->adminBotService.additionalReportDateStateHandler(chatId,text,message.getMessageId(),bot);
                case BASE_MENU -> {
                    switch (text) {
                        case "➕Добавить транзакцию" -> adminBotService.addTransactionHandler(chatId, bot);
                        case "\uD83D\uDC65Управление клиентами" -> adminBotService.clientControlHandler(chatId, bot);
                        case "\uD83C\uDFB0Управление видами услуг" ->
                                adminBotService.serviceControlHandler(chatId, bot);
                        case "\uD83D\uDCC8Управление категориями услуг" ->
                                adminBotService.categoryControlHandler(chatId, bot);
                        case "\uD83D\uDCD1Отчеты" -> adminBotService.reportControlHandler(chatId, bot);
                    }
                }
                case ADD_TRANSACTION -> {
                    switch (text) {
                        case "Доход", "Расход", "Перемещение" ->
                                adminBotService.incomeMessageHandler(chatId, text, bot);
                        case "Назад\uD83D\uDD19" -> adminBotService.baseMenuForBackHandler(chatId, bot);
                    }
                }
                case REPORT_FORM -> {
                    switch (text) {
                        case "Доходы за месяц" -> adminBotService.incomeFilterForLastMonthHandler(chatId, bot);
                        case "Расходы за месяц" -> adminBotService.expenseFilterForLastMonthHandler(chatId, bot);
                        case "Сальдо" -> adminBotService.saldoViewHandler(chatId, bot);
                        case "Дополнительные отчеты" -> adminBotService.additionalReport(chatId, bot);
                        case "Назад\uD83D\uDD19" -> adminBotService.baseMenuForBackHandler(chatId, bot);
                    }
                }
                case ADDITIONAL_FILTER_INCOME -> {
                    switch (text) {
                        case "Назад\uD83D\uDD19" -> adminBotService.reportControlHandler(chatId, bot);
                        case "Нет" -> adminBotService.incomeTransactionListForLastMonthHandler(chatId, bot);
                        case "Клиента" -> adminBotService.incomeTransactionListFilterByClient(chatId, bot);
                        case "Услугу" -> adminBotService.incomeTransactionListFilterByService(chatId, bot);
                        case "Период" -> adminBotService.incomeTransactionListFilterByPeriod(chatId, bot);
                    }
                }
                case ADDITIONAL_FILTER_EXPENSE -> {
                    switch (text) {
                        case "Назад\uD83D\uDD19" -> adminBotService.reportControlHandler(chatId, bot);
                        case "Нет" -> adminBotService.expenseTransactionListForLastMonthHandler(chatId, bot);
                        case "Период" -> adminBotService.expenseTransactionListFilterByPeriod(chatId, bot);
                        case "Категорию расходов" ->
                                adminBotService.expenseTransactionListFilterByServiceOfCategory(chatId, bot);
                    }
                }
                case ADDITIONAL_REPORT -> {
                    switch (text) {
                        case "Назад\uD83D\uDD19" -> adminBotService.reportControlHandler(chatId, bot);
                        case "Типу транзакции" -> adminBotService.additionalReportByTransactionType(chatId,bot);
                        case "Типу денег" -> adminBotService.additionalReportByMoneyType(chatId,bot);
                        case "Периоду" -> adminBotService.additionalReportByPeriod(chatId,bot);
                    }
                }
                case ADDITIONAL_REPORT_BY_TRANSACTION_TYPE->{
                    switch (text){
                        case "Назад\uD83D\uDD19" -> adminBotService.additionalReport(chatId, bot);
                        case "Доход","Расход","Перемещение"->adminBotService.additionalReportListByTransactionType(chatId,text,bot);
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
                    adminBotService.addCategoryHandler(chatId, currentState, bot);
                } else {
                    adminBotService.expenseTypeStateHandler(chatId, data, bot);
                }
            }
            case INCOME_TYPE -> {
                if (data.equals("other")) {
                    adminBotService.addClientHandler(chatId, currentState, bot);
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
            case INCOME_TYPE_STATUS -> adminBotService.incomeTypeStatusStateHandler(chatId, data, bot);
            case CLIENT_LIST -> {
                if (data.equals("other")) {
                    adminBotService.addClientHandler(chatId, currentState, bot);
                } else if (data.equals("back")) {
                    adminBotService.baseMenuForBackHandler(chatId, bot);
                } else {
                    adminBotService.editClientHandler(chatId, data, bot);
                }
            }
            case SERVICE_LIST -> {
                if (data.equals("other")) {
                    adminBotService.addServiceHandler(chatId, currentState, bot);
                } else if (data.equals("back")) {
                    adminBotService.baseMenuForBackHandler(chatId, bot);
                } else {
                    adminBotService.editServiceHandler(chatId, data, bot);
                }
            }
            case CATEGORY_LIST -> {
                if (data.equals("other")) {
                    adminBotService.addCategoryHandler(chatId, currentState, bot);
                } else if (data.equals("back")) {
                    adminBotService.baseMenuForBackHandler(chatId, bot);
                } else {
                    adminBotService.editCategoryHandler(chatId, data, bot);
                }
            }
            case EDIT_CLIENT -> {
                switch (data) {
                    case "edit" ->
                            adminBotService.showClientDetailsHandler(chatId, Sessions.getClientId(chatId).toString(), bot);
                    case "delete" -> adminBotService.deleteClientHandler(chatId, callbackQuery, bot);
                    case "back" -> adminBotService.clientControlHandler(chatId, bot);
                }
            }
            case EDIT_SERVICE_FORM -> {
                switch (data) {
                    case "edit" ->
                            adminBotService.showServiceDetailsHandler(chatId, Sessions.getServiceId(chatId).toString(), bot);
                    case "delete" -> adminBotService.deleteServiceHandler(chatId, callbackQuery, bot);
                    case "back" -> adminBotService.serviceListHandler(chatId, bot);
                }
            }
            case EDIT_CATEGORY_FORM -> {
                switch (data) {
                    case "edit" ->
                            adminBotService.showCategoryDetailsHandler(chatId, Sessions.getCategoryId(chatId).toString(), bot);
                    case "delete" -> adminBotService.deleteCategoryHandler(chatId, callbackQuery, bot);
                    case "back" -> adminBotService.categoryListHandler(chatId, bot);
                }
            }
            case EDIT_CLIENT_FORM -> {
                switch (data) {
                    case "full_name" -> adminBotService.addClientHandler(chatId, currentState, bot);
                    case "phone" -> adminBotService.editClientPhoneNumberHandler(chatId, bot);
                    case "service" -> adminBotService.editClientServiceHandler(chatId, bot);
                    case "back" ->
                            adminBotService.editClientHandler(chatId, Sessions.getClientId(chatId).toString(), bot);
                }
            }
            case EDIT_SERVICE -> {
                switch (data) {
                    case "name" -> adminBotService.addServiceHandler(chatId, currentState, bot);
                    case "back" ->
                            adminBotService.editServiceHandler(chatId, Sessions.getServiceId(chatId).toString(), bot);
                }
            }
            case EDIT_CATEGORY -> {
                switch (data) {
                    case "name" -> adminBotService.addCategoryHandler(chatId, currentState, bot);
                    case "back" ->
                            adminBotService.editCategoryHandler(chatId, Sessions.getCategoryId(chatId).toString(), bot);
                }
            }
            case EDIT_CLIENT_SERVICE -> {
                if (data.equals("other")) {
                    adminBotService.addServiceHandler(chatId, currentState, bot);
                } else {
                    adminBotService.editClientServiceStateHandler(chatId, data, bot);
                }
            }
            case REQUEST_CLIENT_SERVICE_CATEGORY -> {
                if (data.equals("other")) {
                    adminBotService.addServiceHandler(chatId, currentState, bot);
                } else {
                    adminBotService.setCategoryInClientHandler(chatId, data, bot);
                }
            }
            case MONTHLY_REPORT_LIST -> {
                switch (data) {
                    case "back" -> adminBotService.incomeFilterForLastMonthHandler(chatId, bot);
                    case "install" -> adminBotService.installFileIncomeTransactionPdfHandler(chatId, bot);
                }
            }
            case MONTHLY_REPORT_LIST_EXPENSE -> {
                switch (data) {
                    case "back" -> adminBotService.reportControlHandler(chatId, bot);
                    case "install" -> adminBotService.installFileExpenseTransactionPdfHandler(chatId, bot);
                }
            }
            case ADDITIONAL_FILTER_INCOME_CLIENT -> {
                if (data.equals("back")) {
                    adminBotService.incomeFilterForLastMonthHandler(chatId, bot);
                } else {
                    adminBotService.incomeTransactionListFilterByClientHandler(chatId, data, bot);
                }
            }
            case ADDITIONAL_FILTER_INCOME_SERVICE -> {
                if (data.equals("back")) {
                    adminBotService.incomeFilterForLastMonthHandler(chatId, bot);
                } else {
                    adminBotService.incomeTransactionListFilterByServiceHandler(chatId, data, bot);
                }
            }
            case ADDITIONAL_FILTER_INCOME_CATEGORY -> {
                if (data.equals("back")) {
                    adminBotService.expenseFilterForLastMonthHandler(chatId, bot);
                } else {
                    adminBotService.expenseTransactionListFilterByCategoryHandler(chatId, data, bot);
                }
            }
            case FILTER_BY_CLIENT_REPORT_LIST -> {
                switch (data) {
                    case "back" -> adminBotService.incomeFilterForLastMonthHandler(chatId, bot);
                    case "install" -> adminBotService.installFileIncomeTransactionByClientPdfHandler(chatId, bot);
                }
            }
            case FILTER_BY_SERVICE_REPORT_LIST -> {
                switch (data) {
                    case "back" -> adminBotService.incomeFilterForLastMonthHandler(chatId, bot);
                    case "install" -> adminBotService.installFileIncomeTransactionByServicePdfHandler(chatId, bot);
                }
            }
            case FILTER_BY_CATEGORY_REPORT_LIST -> {
                switch (data) {
                    case "back" -> adminBotService.expenseFilterForLastMonthHandler(chatId, bot);
                    case "install" -> adminBotService.installFileExpenseTransactionByCategoryPdfHandler(chatId, bot);
                }
            }
            case FILTER_BY_PERIOD_REPORT_LIST -> {
                switch (data) {
                    case "back" -> adminBotService.incomeFilterForLastMonthHandler(chatId, bot);
                    case "install" -> adminBotService.installFileIncomeTransactionByPeriodPdfHandler(chatId, bot);
                }
            }
            case FILTER_BY_PERIOD_EXPENSE_REPORT_LIST -> {
                switch (data) {
                    case "back" -> adminBotService.expenseFilterForLastMonthHandler(chatId, bot);
                    case "install" -> adminBotService.installFileExpenseTransactionByPeriodPdfHandler(chatId, bot);
                }
            }
            case REPORT_LIST_BY_PERIOD->{
                switch (data){
                    case "back"->adminBotService.additionalReport(chatId, bot);
                    case "install"->adminBotService.installAdditionalReportByPeriod(chatId,bot);
                }
            }
            case ADDITIONAL_REPORT_BY_MONEY_TYPE->{
                if (data.equals("back")){
                    adminBotService.additionalReport(chatId, bot);
                }else {
                    adminBotService.showAdditionalReportByMoneyType(chatId,data,bot);
                }
            }
            case REPORT_LIST_BY_MONEY_TYPE->{
                switch (data){
                    case "back"->adminBotService.additionalReportByMoneyType(chatId, bot);
                    case "install"->adminBotService.installAdditionalReportByMoneyType(chatId,bot);
                }
            }
            case REPORT_LIST_BY_TRANSACTION_TYPE->{
                switch (data){
                    case "back"->adminBotService.additionalReportByTransactionType(chatId,bot);
                    case "install"->adminBotService.installAdditionalReportByTransactionType(chatId,bot);
                }
            }
        }
    }
}


