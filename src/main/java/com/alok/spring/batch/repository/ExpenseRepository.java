package com.alok.spring.batch.repository;

import com.alok.spring.batch.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT * FROM expense WHERE to_char(DATE, 'YYYYMM') = to_char(sysdate, 'YYYYMM')")
    List<Expense> findAllForCurrentMonth();
}
