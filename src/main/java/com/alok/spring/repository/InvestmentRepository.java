package com.alok.spring.repository;

import com.alok.spring.model.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {
}

