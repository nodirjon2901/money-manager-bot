package uz.result.moneymanagerbot.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.result.moneymanagerbot.model.Transaction;
import uz.result.moneymanagerbot.model.TransactionType;
import uz.result.moneymanagerbot.model.UserState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Modifying
    @Transactional
    @Query(value = "update transactions set money_type=:moneyType where id=:id", nativeQuery = true)
    void updateTransactionMoneyTypeById(@Param("moneyType") String moneyType, @Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "update transactions set summa=:summa where id=:id", nativeQuery = true)
    void updateTransactionSummaById(@Param("summa") Double summa, @Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "update transactions set transaction_date=:date where id=:id", nativeQuery = true)
    void updateTransactionDateById(@Param("date") LocalDate date, @Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "update transactions set comment=:comment where id=:id", nativeQuery = true)
    void updateTransactionCommentById(@Param("comment") String comment, @Param("id") Long id);

    @Query(value = "select transaction_type from transactions where id=:id", nativeQuery = true)
    Optional<TransactionType> getTransactionTypeById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "update transactions set transaction_status=:status where id=:id", nativeQuery = true)
    void updateTransactionStatusById(@Param("status") String status, @Param("id") Long id);

    @Query(value = "SELECT * FROM transactions t WHERE t.transaction_type = 'INCOME' AND t.transaction_date >= :startDate AND t.transaction_date <= :endDate", nativeQuery = true)
    List<Transaction> findAllIncomeTransactionsWithinOneMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT * FROM transactions t WHERE t.transaction_type = 'EXPENSE' AND t.transaction_date >= :startDate AND t.transaction_date <= :endDate", nativeQuery = true)
    List<Transaction> findAllExpenseTransactionsWithinOneMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


}
