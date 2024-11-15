package uz.result.moneymanagerbot.bot;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.result.moneymanagerbot.model.Client;
import uz.result.moneymanagerbot.model.ExpenseCategory;
import uz.result.moneymanagerbot.model.MoneyType;

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
        KeyboardButton b4 = new KeyboardButton("\uD83D\uDCD1Отчеты");
        row.add(b4);
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

    public InlineKeyboardMarkup expenseTypeInlineMarkup(List<ExpenseCategory> categoryList) {
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

    public InlineKeyboardMarkup incomeTypeInlineMarkup(List<Client> clientList) {
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

        buttonRow=new ArrayList<>();
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
}
