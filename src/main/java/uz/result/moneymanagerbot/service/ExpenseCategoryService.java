package uz.result.moneymanagerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.result.moneymanagerbot.exceptions.NotFoundException;
import uz.result.moneymanagerbot.model.ExpenseCategory;
import uz.result.moneymanagerbot.repository.ExpenseCategoryRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository categoryRepository;

    public ExpenseCategory save(ExpenseCategory category) {
        if (categoryRepository.existsByName(category.getName()))
            return null;
        return categoryRepository.save(category);
    }

    public List<ExpenseCategory> findAll() {
        return categoryRepository.findAll();
    }

    public void defaultCategorySave() {
        List<ExpenseCategory> categoryList = new ArrayList<>();
        if (!categoryRepository.existsByName("ЗП (зарплата)"))
            categoryList.add(ExpenseCategory.builder().name("ЗП (зарплата)").build());
        if (!categoryRepository.existsByName("Дивиденды"))
            categoryList.add(ExpenseCategory.builder().name("Дивиденды").build());
        if (!categoryRepository.existsByName("Реклама"))
            categoryList.add(ExpenseCategory.builder().name("Реклама").build());
        if (!categoryRepository.existsByName("Реклама клиентов)"))
            categoryList.add(ExpenseCategory.builder().name("Реклама клиентов)").build());
        if (!categoryRepository.existsByName("Фонд развития"))
            categoryList.add(ExpenseCategory.builder().name("Фонд развития").build());
        if (!categoryRepository.existsByName("Фискальные расходы"))
            categoryList.add(ExpenseCategory.builder().name("Фискальные расходы").build());
        if (!categoryRepository.existsByName("Хоз. Расходы"))
            categoryList.add(ExpenseCategory.builder().name("Хоз. Расходы").build());
        if (!categoryRepository.existsByName("Услуги"))
            categoryList.add(ExpenseCategory.builder().name("Услуги").build());
        if (!categoryRepository.existsByName("Профсоюз"))
            categoryList.add(ExpenseCategory.builder().name("Профсоюз").build());
        if (!categoryRepository.existsByName("Комиссионные"))
            categoryList.add(ExpenseCategory.builder().name("Комиссионные").build());

        if (!categoryList.isEmpty())
            categoryRepository.saveAll(categoryList);
    }

    public ExpenseCategory findById(Integer id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category is not found with id: " + id));
    }

}
