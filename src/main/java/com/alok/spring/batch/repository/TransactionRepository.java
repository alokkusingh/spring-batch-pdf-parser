package com.alok.spring.batch.repository;

import com.alok.spring.batch.model.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.crypto.spec.OAEPParameterSpec;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query("SELECT t FROM Transaction t WHERE t.head IS NOT NULL")
    List<Transaction> findAll();

    @Query(value = "SELECT MAX(DATE) FROM TRANSACTION", nativeQuery = true)
    Optional<Date>  findLastTransactionDate();
}
