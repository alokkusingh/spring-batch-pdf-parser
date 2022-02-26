package com.alok.spring.batch.repository;

import com.alok.spring.batch.model.Expense;
import com.alok.spring.batch.model.ExpenseCategorySum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE to_char(e.date, 'YYYYMM') = to_char(sysdate, 'YYYYMM')")
    List<Expense> findAllForCurrentMonth();

    @Query(value = "select new com.alok.spring.batch.model.ExpenseCategorySum(to_char(e.date, 'YYYYMM') month, e.category, SUM(e.amount) sum) from " +
            "Expense e group by month, e.category order by month desc, sum desc")
    //@Query(value = "select to_char(e.date, 'YYYYMM') month, e.category, SUM(e.amount) sum from " +
    //        "Expense e group by month, e.category order by month desc, sum desc", nativeQuery = true)
    List<ExpenseCategorySum> findSumGroupByMonth();
}
