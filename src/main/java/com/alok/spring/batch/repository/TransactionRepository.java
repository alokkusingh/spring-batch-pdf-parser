package com.alok.spring.batch.repository;

import com.alok.spring.batch.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
}
