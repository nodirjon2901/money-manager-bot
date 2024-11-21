package uz.result.moneymanagerbot.bot;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.result.moneymanagerbot.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarkupService {


    public ReplyKeyboard baseMenuReplyMarkupService() {
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton b1 = new KeyboardButton("➕Добавить транзакцию");
        row.add(b1);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton b2 = new KeyboardButton("\uD83D\uDC65Управление клиентами");
        row.add(b2);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton b3 = new KeyboardButton("\uD83D\uDCC8Управление категориями услуг");
        row.add(b3);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton b31 = new KeyboardButton("\uD83C\uDFB0Управление видами услуг");
        row.add(b31);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton b4 = new KeyboardButton("\uD83D\uDCD1Отчеты");
        row.add(b4);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton a1 = new KeyboardButton("Доход");
        row.add(a1);

        KeyboardButton a2 = new KeyboardButton("Расход");
        row.add(a2);

        KeyboardButton a3 = new KeyboardButton("Перемещение");
        row.add(a3);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton balance = new KeyboardButton("\uD83D\uDCB3Баланс");
        row.add(balance);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton b5 = new KeyboardButton("⚙️Настройки и доступы");
        row.add(b5);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton b6 = new KeyboardButton("✍️Уведомления");
        row.add(b6);
        rows.add(row);

        replyKeyboard.setKeyboard(rows);
        return replyKeyboard;
    }

    public ReplyKeyboard transactionTypeReplyMarkup() {
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton b1 = new KeyboardButton("Доход");
        row.add(b1);

        KeyboardButton b2 = new KeyboardButton("Расход");
        row.add(b2);

        KeyboardButton b3 = new KeyboardButton("Перемещение");
        row.add(b3);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton back = new KeyboardButton("Назад\uD83D\uDD19");
        row.add(back);
        rows.add(row);
        replyKeyboard.setKeyboard(rows);
        return replyKeyboard;
    }

    public InlineKeyboardMarkup moneyTypeListInlineMarkup(Long transactionId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Нал суммы");
        button.setCallbackData(MoneyType.CASH_AMOUNT.name() + "_" + transactionId);
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Нал валюта");
        button.setCallbackData(MoneyType.CASH_CURRENCY.name() + "_" + transactionId);
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Банк");
        button.setCallbackData(MoneyType.BANK.name() + "_" + transactionId);
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Карта суммы");
        button.setCallbackData(MoneyType.CARD_AMOUNT1.name() + "_" + transactionId);
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Карта суммы 2");
        button.setCallbackData(MoneyType.CARD_AMOUNT_2.name() + "_" + transactionId);
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Карта суммы 3");
        button.setCallbackData(MoneyType.CARD_AMOUNT_3.name() + "_" + transactionId);
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Visa");
        button.setCallbackData(MoneyType.VISA.name() + "_" + transactionId);
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Назад\uD83D\uDD19");
        button.setCallbackData("back_" + transactionId);
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public ReplyKeyboardRemove removeReplyMarkup() {
        return new ReplyKeyboardRemove(true);
    }

    public InlineKeyboardMarkup transactionDateInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Текущая дата");
        button.setCallbackData("now");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Другой");
        button.setCallbackData("other");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup categoryListInlineMarkup(List<ExpenseCategory> categoryList, UserState state) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        for (ExpenseCategory category : categoryList) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttonRow = new ArrayList<>();
            button.setText(category.getName());
            button.setCallbackData(category.getId().toString());
            buttonRow.add(button);
            rowsInline.add(buttonRow);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Другое ➕");
        button.setCallbackData("other");
        buttonRow.add(button);

        if (!state.equals(UserState.TRANSACTION_DATE)) {
            button = new InlineKeyboardButton();
            button.setText("Назад \uD83D\uDD19");
            button.setCallbackData("back");
            buttonRow.add(button);
        }
        rowsInline.add(buttonRow);
        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup commentInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("ДА");
        button.setCallbackData("yes");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("НЕТ");
        button.setCallbackData("no");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup informationRequestFormInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Подтверждение✅");
        button.setCallbackData("confirm");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Отмена❌");
        button.setCallbackData("cancellation");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup clientListInlineMarkup(List<Client> clientList) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        for (Client client : clientList) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttonRow = new ArrayList<>();
            button.setText(client.getFullName());
            button.setCallbackData(client.getId().toString());
            buttonRow.add(button);
            rowsInline.add(buttonRow);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Другое ➕");
        button.setCallbackData("other");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Назад \uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup incomeStatusInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Предоплата");
        button.setCallbackData("PRE_PAYMENT");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Запланировано");
        button.setCallbackData("SCHEDULED");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        buttonRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Финзайм");
        button.setCallbackData("FINANCIAL_LOAN");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Постоплата");
        button.setCallbackData("POST_PAYMENT");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup clientServiceListInlineMarkup(List<ServiceType> list) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        for (ServiceType type : list) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttonRow = new ArrayList<>();
            button.setText(type.getName());
            button.setCallbackData(type.getId().toString());
            buttonRow.add(button);
            rowsInline.add(buttonRow);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Другое ➕");
        button.setCallbackData("other");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup clientSettingInlineMarkupService() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("ФИО");
        button.setCallbackData("full_name");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Номер телефона");
        button.setCallbackData("phone");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        buttonRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Услугу ");
        button.setCallbackData("service");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Назад \uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;

    }

    public InlineKeyboardMarkup selectedCategoryInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Редактировать ✍️");
        button.setCallbackData("edit");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Удалить \uD83D\uDDD1");
        button.setCallbackData("delete");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Назад \uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup serviceListInlineMarkup(List<ServiceType> serviceList) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        for (ServiceType type : serviceList) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttonRow = new ArrayList<>();
            button.setText(type.getName());
            button.setCallbackData(type.getId().toString());
            buttonRow.add(button);
            rowsInline.add(buttonRow);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Другое ➕");
        button.setCallbackData("other");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Назад \uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup serviceSettingInlineMarkupService() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Название ");
        button.setCallbackData("name");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Назад \uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public ReplyKeyboard reportFormReplyMarkup() {
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton b1 = new KeyboardButton("Доходы за месяц");
        row.add(b1);

        KeyboardButton b2 = new KeyboardButton("Расходы за месяц");
        row.add(b2);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton b3 = new KeyboardButton("Сальдо");
        row.add(b3);

        KeyboardButton b4 = new KeyboardButton("Дополнительные отчеты");
        row.add(b4);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton back = new KeyboardButton("Назад\uD83D\uDD19");
        row.add(back);
        rows.add(row);
        replyKeyboard.setKeyboard(rows);
        return replyKeyboard;
    }

    public ReplyKeyboard additionalReportReplyMarkup() {
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton b1 = new KeyboardButton("Клиента");
        row.add(b1);

        KeyboardButton b2 = new KeyboardButton("Услугу");
        row.add(b2);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton b3 = new KeyboardButton("Период");
        row.add(b3);

        KeyboardButton b4 = new KeyboardButton("Нет");
        row.add(b4);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton back = new KeyboardButton("Назад\uD83D\uDD19");
        row.add(back);
        rows.add(row);
        replyKeyboard.setKeyboard(rows);
        return replyKeyboard;
    }

    public ReplyKeyboard additionalExpenseReportReplyMarkup() {
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton b1 = new KeyboardButton("Категорию расходов");
        row.add(b1);

        KeyboardButton b2 = new KeyboardButton("Период");
        row.add(b2);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton b3 = new KeyboardButton("Нет");
        row.add(b3);

        KeyboardButton back = new KeyboardButton("Назад\uD83D\uDD19");
        row.add(back);
        rows.add(row);
        replyKeyboard.setKeyboard(rows);
        return replyKeyboard;
    }

    public ReplyKeyboard clientListForFilterInlineMarkup(List<Client> clientList) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        for (Client client : clientList) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttonRow = new ArrayList<>();
            button.setText(client.getFullName());
            button.setCallbackData(client.getId().toString());
            buttonRow.add(button);
            rowsInline.add(buttonRow);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Назад \uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public ReplyKeyboard serviceListForFilterInlineMarkup(List<ServiceType> typeList) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        for (ServiceType type : typeList) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttonRow = new ArrayList<>();
            button.setText(type.getName());
            button.setCallbackData(type.getId().toString());
            buttonRow.add(button);
            rowsInline.add(buttonRow);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Назад \uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public ReplyKeyboard serviceOfCategoryListForFilterInlineMarkup(List<ExpenseCategory> categoryList) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        for (ExpenseCategory category : categoryList) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttonRow = new ArrayList<>();
            button.setText(category.getName());
            button.setCallbackData(category.getId().toString());
            buttonRow.add(button);
            rowsInline.add(buttonRow);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Назад \uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public ReplyKeyboard additionalFilterReportReplyMarkup() {
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton b1 = new KeyboardButton("Типу транзакции");
        row.add(b1);

        KeyboardButton b2 = new KeyboardButton("Периоду");
        row.add(b2);
        rows.add(row);

        row = new KeyboardRow();
        KeyboardButton b3 = new KeyboardButton("Типу денег");
        row.add(b3);

        KeyboardButton back = new KeyboardButton("Назад\uD83D\uDD19");
        row.add(back);
        rows.add(row);
        replyKeyboard.setKeyboard(rows);
        return replyKeyboard;
    }

    public InlineKeyboardMarkup moneyTypeListInlineMarkupKeyBoard() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Нал суммы");
        button.setCallbackData(MoneyType.CASH_AMOUNT.name());
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Нал валюта");
        button.setCallbackData(MoneyType.CASH_CURRENCY.name());
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Банк");
        button.setCallbackData(MoneyType.BANK.name());
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Карта суммы");
        button.setCallbackData(MoneyType.CARD_AMOUNT1.name());
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Карта суммы 2");
        button.setCallbackData(MoneyType.CARD_AMOUNT_2.name());
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Карта суммы 3");
        button.setCallbackData(MoneyType.CARD_AMOUNT_3.name());
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Visa");
        button.setCallbackData(MoneyType.VISA.name());
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Назад\uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public ReplyKeyboard settingsFormReplyMarkup(UserRole role) {
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
        if (role.equals(UserRole.SUPPER_ADMIN)) {
            KeyboardRow row = new KeyboardRow();
            KeyboardButton b1 = new KeyboardButton("Пользователи");
            row.add(b1);

            KeyboardButton b2 = new KeyboardButton("Просмотр текущих доступов");
            row.add(b2);

            KeyboardButton b3 = new KeyboardButton("Изменение пароля");
            row.add(b3);
            rows.add(row);
        }
        else {
            KeyboardRow row = new KeyboardRow();
            KeyboardButton b1 = new KeyboardButton("Просмотр текущих доступов");
            row.add(b1);

            KeyboardButton b2 = new KeyboardButton("Изменение пароля");
            row.add(b2);
            rows.add(row);
        }
        KeyboardRow row = new KeyboardRow();
        KeyboardButton back = new KeyboardButton("Назад\uD83D\uDD19");
        row.add(back);
        rows.add(row);
        replyKeyboard.setKeyboard(rows);
        return replyKeyboard;
    }

    public InlineKeyboardMarkup userListInlineMarkup(List<User> userList) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        for (User user : userList) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttonRow = new ArrayList<>();
            button.setText(user.getName());
            button.setCallbackData(user.getId().toString());
            buttonRow.add(button);
            rowsInline.add(buttonRow);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Другое ➕");
        button.setCallbackData("other");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Назад \uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup roleInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        buttonRow = new ArrayList<>();
        button.setText("Администратор ");
        button.setCallbackData("ADMIN");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Наблюдатель");
        button.setCallbackData("OBSERVER");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public InlineKeyboardMarkup userEditFormInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Имя");
        button.setCallbackData("name");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("ROLE");
        button.setCallbackData("role");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        buttonRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("ChatId ");
        button.setCallbackData("chat_id");
        buttonRow.add(button);

        button = new InlineKeyboardButton();
        button.setText("Назад \uD83D\uDD19");
        button.setCallbackData("back");
        buttonRow.add(button);
        rowsInline.add(buttonRow);

        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }
}
