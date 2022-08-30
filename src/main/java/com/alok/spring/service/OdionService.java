package com.alok.spring.service;

import com.alok.spring.model.OdionTransaction;
import com.alok.spring.repository.OdionTransactionRepository;
import com.alok.spring.response.GetOdionTransactionsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OdionService {

    private final OdionTransactionRepository odionTransactionRepository;

    public OdionService(OdionTransactionRepository odionTransactionRepository) {
        this.odionTransactionRepository = odionTransactionRepository;
    }

    public GetOdionTransactionsResponse getAllTransactions() {
        log.info("All Taxes not available in cache");

        return GetOdionTransactionsResponse.builder()
                .transactions(odionTransactionRepository.findAll().stream()
                        .filter(transaction -> transaction.getAmount() > 0)
                        .toList())
                .build();
    }

    @Transactional
    public void saveAllTransactions(List<OdionTransaction> transactions) {
        log.info("Delete all the Odion transactions first");
        odionTransactionRepository.deleteAll();

        log.info("Save all the Odion transactions");
        odionTransactionRepository.saveAll(transactions);
    }
}
