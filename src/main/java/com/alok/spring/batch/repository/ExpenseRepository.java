package com.alok.spring.batch.repository;

import com.alok.spring.batch.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
