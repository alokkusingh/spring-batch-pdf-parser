package com.alok.spring.batch.repository;

import com.alok.spring.batch.model.Expense;
import com.alok.spring.batch.model.IExpenseCategoryMonthSum;
import com.alok.spring.batch.model.IExpenseMonthSum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    //@Query("SELECT e FROM Expense e WHERE to_char(e.date, 'YYYYMM') = to_char(sysdate, 'YYYYMM')")
    @Query("SELECT e FROM Expense e WHERE e.year = ?1 and e.month = ?2")
    List<Expense> findAllForCurrentMonth(Integer year, Integer month);

    //@Query(value = "select new com.alok.spring.batch.model.ExpenseCategorySum(to_char(e.date, 'YYYYMM') month, e.category, SUM(e.amount) sum) from " +
    //        "Expense e group by month, e.category order by month desc, sum desc")
    @Query(value = "select year, month, e.category, SUM(e.amount) sum from " +
            "expense e group by year, month, e.category order by year desc, month desc, sum desc", nativeQuery = true)
    List<IExpenseCategoryMonthSum> findCategorySumGroupByMonth();

    @Query(value = "select year, month, SUM(e.amount) sum from " +
            "expense e group by year, month order by year desc, month desc, sum desc", nativeQuery = true)
    List<IExpenseMonthSum> findSumGroupByMonth();

    @Query(value = "SELECT MAX(DATE) FROM expense", nativeQuery = true)
    Optional<LocalDate> findLastTransactionDate();


}

