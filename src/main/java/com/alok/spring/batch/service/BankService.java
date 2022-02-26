package com.alok.spring.batch.service;

import com.alok.spring.batch.exception.ResourceNotFoundException;
import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.repository.TransactionRepository;
import com.alok.spring.batch.response.GetTransactionResponse;
import com.alok.spring.batch.response.GetTransactionsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BankService {

    @Autowired
    private TransactionRepository transactionRepository;

    public GetTransactionsResponse getAllTransactions() {


        List<Transaction> transactions = transactionRepository.findAll();
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
                .build();
    }

    public GetTransactionResponse getTransaction(Integer id) {
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
