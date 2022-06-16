package com.alok.spring.service;

import com.alok.spring.config.CacheConfig;
import com.alok.spring.exception.ResourceNotFoundException;
import com.alok.spring.model.Transaction;
import com.alok.spring.repository.TransactionRepository;
import com.alok.spring.response.GetSalaryByCompanyResponse;
import com.alok.spring.response.GetTransactionResponse;
import com.alok.spring.response.GetTransactionsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
                        .subHead(transaction.getSubHead())
                        .credit(transaction.getCredit())
                        .debit(transaction.getDebit())
                        .bank(transaction.getBank())
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
                .subHead(transaction.getSubHead())
                .credit(transaction.getCredit())
                .debit(transaction.getDebit())
                .description(transaction.getDescription())
                .bank(transaction.getBank())
                .build();
    }

    //@Cacheable(CacheConfig.CacheName.TRANSACTION)
    public GetSalaryByCompanyResponse getSalaryByCompany() {

        log.info("All Transactions not available in cache");

        SimpleDateFormat dfYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dfMonth = new SimpleDateFormat("MM");

        List<Transaction> salaryTransactions = transactionRepository.findSalaryTransactions();

        Map<String, Map<GetSalaryByCompanyResponse.CompanySalary.MonthSalary, Integer>> salaryByCompanyMonth = salaryTransactions.stream()
                .collect(
                        Collectors.groupingBy(Transaction::getSubHead,
                                Collectors.groupingBy(
                                        transaction -> GetSalaryByCompanyResponse.CompanySalary.MonthSalary.builder()
                                                .year(Integer.valueOf(dfYear.format(transaction.getDate())))
                                                .month(Integer.valueOf(dfMonth.format(transaction.getDate())))
                                                .build(),
                                        Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                monthSalaryTransactions -> monthSalaryTransactions.stream()
                                                        .map(Transaction::getDebit)
                                                        .reduce(0, Integer::sum)
                                        )
                                )
                        )
                );

        GetSalaryByCompanyResponse response = GetSalaryByCompanyResponse.builder()
                .companySalaries(new ArrayList<>(salaryByCompanyMonth.size()))
                .build();

        AtomicInteger total = new AtomicInteger(0);
        salaryByCompanyMonth.forEach((company, monthSalaryAndAmount) -> {
            AtomicInteger companyTotal = new AtomicInteger(0);
            monthSalaryAndAmount.forEach((monthlySalary, amount) -> {
                monthlySalary.setAmount(amount);
                companyTotal.addAndGet(amount);
                total.addAndGet(amount);
            });

            List<GetSalaryByCompanyResponse.CompanySalary.MonthSalary> monthlySalaries = new ArrayList<>(
                    monthSalaryAndAmount.keySet()
            );

            Collections.sort(monthlySalaries);

            response.getCompanySalaries().add(GetSalaryByCompanyResponse.CompanySalary.builder()
                    .company(company)
                    .monthSalaries(monthlySalaries)
                    .total(companyTotal.get())
                    .build());
        });

        response.setTotal(total.get());

        return response;
    }
}
