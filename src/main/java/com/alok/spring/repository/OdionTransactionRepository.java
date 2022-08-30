package com.alok.spring.repository;

import com.alok.spring.model.OdionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OdionTransactionRepository extends JpaRepository<OdionTransaction, Integer> {
}

