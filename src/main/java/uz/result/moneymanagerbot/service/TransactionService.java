package uz.result.moneymanagerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.result.moneymanagerbot.exceptions.NotFoundException;
import uz.result.moneymanagerbot.model.*;
import uz.result.moneymanagerbot.repository.TransactionRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final FileService fileService;

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction is not found with id: " + id));
    }

    public void updateTransactionMoneyTypeById(Long id, String moneyType) {
        transactionRepository.updateTransactionMoneyTypeById(moneyType, id);
    }

    public void updateTransactionSummaById(Long id, Double summa) {
        transactionRepository.updateTransactionSummaById(summa, id);
    }

    public void updateTransactionDateById(Long id, LocalDate date) {
        transactionRepository.updateTransactionDateById(date, id);
    }

    public void updateTransactionCommentById(Long id, String comment) {
        transactionRepository.updateTransactionCommentById(comment, id);
    }

    public void updateTransactionStatusById(Long id, String transactionStatus) {
        transactionRepository.updateTransactionStatusById(transactionStatus, id);
    }

    public TransactionType getTransactionTypeById(Long id) {
        return transactionRepository.getTransactionTypeById(id)
                .orElseThrow(() -> new NotFoundException("Transaction is not found with id:" + id));
    }

    public void updateTransactionExpenseCategoryById(Long id, ExpenseCategory category) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new NotFoundException("Transaction is not found with id: " + id));
        transaction.setExpenseCategory(category);
        transactionRepository.save(transaction);
    }

    public void updateTransactionFileById(Long id, FileEntity file) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new NotFoundException("Transaction is not found with id: " + id));
        transaction.setFile(file);
        transactionRepository.save(transaction);

    }

    public void deleteById(Long id) throws IOException {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new NotFoundException("Transaction is not found with id: " + id));
        if (transaction.getFile() != null) {
            fileService.deleteFromFile(transaction.getFile().getSystemPath());
        }
        transactionRepository.delete(transaction);
    }

    public void updateTransactionClientById(Long id, Client client) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction is not found with id: " + id));
        transaction.setClient(client);
        transactionRepository.save(transaction);
    }

    public List<Transaction> getIncomeTransactionsForLastMonth() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        return transactionRepository.findAllIncomeTransactionsWithinOneMonth(startDate, endDate);
    }

    public List<Transaction> getExpenseTransactionsForLastMonth() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        return transactionRepository.findAllExpenseTransactionsWithinOneMonth(startDate,endDate);
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public List<Transaction> findAllIncomeTransactionsWithClientId(Long clientId) {
        return transactionRepository.findAllIncomeTransactionsWithClientId(clientId);
    }

    public List<Transaction> findAllIncomeTransactionsWithClientService(Integer serviceId) {
        return transactionRepository.findAllIncomeTransactionsWithClientServiceId(serviceId);
    }

    public List<Transaction> findAllIncomeTransactionsWithPeriod(String period) {
        LocalDate[] dates = parseDateRange(period);
        LocalDate startDate = dates[0];
        LocalDate endDate = dates[1];
        return transactionRepository.findAllIncomeTransactionsWithinOneMonth(startDate, endDate);
    }

    public List<Transaction> findAllExpenseTransactionsWithPeriod(String period) {
        LocalDate[] dates = parseDateRange(period);
        LocalDate startDate = dates[0];
        LocalDate endDate = dates[1];
        return transactionRepository.findAllExpenseTransactionsWithinOneMonth(startDate,endDate);
    }

    private LocalDate[] parseDateRange(String dateRange) {
        if (dateRange == null || !dateRange.contains("/")) {
            return null;
        }

        String[] dates = dateRange.split("/");
        if (dates.length != 2) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate startDate = LocalDate.parse(dates[0], formatter);
            LocalDate endDate = LocalDate.parse(dates[1], formatter);
            return new LocalDate[]{startDate, endDate};
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public List<Transaction> findAllExpenseTransactionsWithClientCategory(Integer id) {
        return transactionRepository.findAllExpenseTransactionsWithCategoryId(id);
    }
}
