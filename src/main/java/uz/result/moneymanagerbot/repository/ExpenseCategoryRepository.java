package uz.result.moneymanagerbot.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.result.moneymanagerbot.model.ExpenseCategory;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Integer> {

    boolean existsById(Integer id);

    boolean existsByName(String name);

    @Modifying
    @Transactional
    @Query(value = "update expense_categories set name=:name where id=:id", nativeQuery = true)
    void updateExpenseCategoryNameById(@Param("name") String name, @Param("id") Integer id);

}
