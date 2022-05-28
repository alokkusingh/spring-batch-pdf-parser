package com.alok.spring.service;

import com.alok.spring.config.CacheConfig;
import com.alok.spring.exception.ResourceNotFoundException;
import com.alok.spring.model.Transaction;
import com.alok.spring.repository.TransactionRepository;
import com.alok.spring.response.GetTransactionResponse;
import com.alok.spring.response.GetTransactionsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BankService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Cacheable(CacheConfig.CacheName.TRANSACTION)
    public GetTransactionsResponse getAllTransactions() {
        log.info("All Transactions not available in cache");

        List<Transaction> transactions = transactionRepository.findAll();
        Date lastTransactionDate = transactionRepository.findLastTransactionDate()
                .orElse(new Date());
        Collections.sort(transactions, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        List<GetTransactionsResponse.Transaction> transactionsList = transactions.stream()
                .map(transaction -> GetTransactionsResponse.Transaction.builder()
                        .id(transaction.getId())
                        .date(transaction.getDate())
                        .head(transaction.getHead())
                        .credit(transaction.getCredit())
                        .debit(transaction.getDebit())
                        .build())
                .collect(Collectors.toList());

        return GetTransactionsResponse.builder()
                .transactions(transactionsList)
                .count(transactionsList.size())
                .lastTransactionDate(lastTransactionDate)
                .build();
    }

    @Cacheable(CacheConfig.CacheName.TRANSACTION)
    public GetTransactionResponse getTransaction(Integer id) {
        log.info("Transaction by id not available in cache");
        Transaction transaction = transactionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found!"));

        return GetTransactionResponse.builder()
                .id(transaction.getId())
                .date(transaction.getDate())
                .head(transaction.getHead())
                .credit(transaction.getCredit())
                .debit(transaction.getDebit())
                .description(transaction.getDescription())
                .build();
    }
}
