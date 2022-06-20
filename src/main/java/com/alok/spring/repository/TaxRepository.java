package com.alok.spring.repository;

import com.alok.spring.model.Tax;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRepository extends JpaRepository<Tax, Long> {
}

