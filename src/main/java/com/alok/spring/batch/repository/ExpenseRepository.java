package com.alok.spring.batch.repository;

import com.alok.spring.batch.model.Expense;
import com.alok.spring.batch.model.ExpenseCategorySum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE to_char(e.date, 'YYYYMM') = to_char(sysdate, 'YYYYMM')")
    List<Expense> findAllForCurrentMonth();

    @Query(value = "select to_char(e.date, 'YYYYMM') mon, e.category, SUM(e.amount) sum from " +
            "Expense e group by mon, e.category order by mon desc, sum desc", nativeQuery = true)
    List<ExpenseCategorySum> findSumGroupByMonth();

}
