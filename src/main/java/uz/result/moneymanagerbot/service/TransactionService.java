package uz.result.moneymanagerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.result.moneymanagerbot.exceptions.NotFoundException;
import uz.result.moneymanagerbot.model.*;
import uz.result.moneymanagerbot.repository.TransactionRepository;

import java.io.IOException;

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

    public void updateTransactionDateById(Long id, String date) {
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
}
