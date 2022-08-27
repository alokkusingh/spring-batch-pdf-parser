package com.alok.spring.repository;

import com.alok.spring.model.Expense;
import com.alok.spring.model.IExpenseCategoryMonthSum;
import com.alok.spring.model.IExpenseMonthSum;
import com.alok.spring.model.YearMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    //@Query("SELECT e FROM Expense e WHERE to_char(e.date, 'YYYYMM') = to_char(sysdate, 'YYYYMM')")
    @Query("SELECT e FROM Expense e WHERE e.year = ?1 and e.month = ?2")
    List<Expense> findAllForMonth(Integer year, Integer month);

    @Query("SELECT e FROM Expense e WHERE e.year = ?1 and e.month = ?2 and e.category = ?3")
    List<Expense> findAllForMonthAndCategory(Integer year, Integer month, String category);

    //@Query(value = "select new com.alok.spring.batch.model.ExpenseCategorySum(to_char(e.date, 'YYYYMM') month, e.category, SUM(e.amount) sum) from " +
    //        "Expense e group by month, e.category order by month desc, sum desc")
    @Query(value = "select year, month, e.category, SUM(e.amount) sum from " +
            "expense e group by year, month, e.category order by year desc, month desc, sum desc", nativeQuery = true)
    List<IExpenseCategoryMonthSum> findCategorySumGroupByMonth();

    @Query(value = "select year, month, e.category, SUM(e.amount) sum from " +
            "expense e WHERE e.category = ?1 group by year, month, e.category order by year desc, month desc, sum desc", nativeQuery = true)
    List<IExpenseCategoryMonthSum> findMonthlyExpenseForCategory(String category);

    @Query(value = "select year, month, SUM(e.amount) sum from " +
            "expense e group by year, month order by year desc, month desc, sum desc", nativeQuery = true)
    List<IExpenseMonthSum> findSumGroupByMonth();

    @Query(value = "SELECT MAX(DATE) FROM expense", nativeQuery = true)
    Optional<Date> findLastTransactionDate();

    @Query("SELECT e FROM Expense e WHERE e.category = ?1")
    List<Expense> findAllForCategory(String category);

    @Query("SELECT DISTINCT category from Expense")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT new com.alok.spring.model.YearMonth(year, month) from Expense")
    List<YearMonth> findDistinctYearMonths();

}

