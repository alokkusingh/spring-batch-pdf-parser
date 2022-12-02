package com.alok.spring.service;

import com.alok.spring.config.CacheConfig;
import com.alok.spring.model.IExpenseMonthSum;
import com.alok.spring.model.Investment;
import com.alok.spring.model.Transaction;
import com.alok.spring.repository.ExpenseRepository;
import com.alok.spring.repository.InvestmentRepository;
import com.alok.spring.repository.TransactionRepository;
import com.alok.spring.response.GetMonthlySummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SummaryService {

    private ExpenseRepository expenseRepository;
    private TransactionRepository transactionRepository;
    private InvestmentRepository investmentRepository;

    public SummaryService(ExpenseRepository expenseRepository, TransactionRepository transactionRepository, InvestmentRepository investmentRepository) {
        this.expenseRepository = expenseRepository;
        this.transactionRepository = transactionRepository;
        this.investmentRepository = investmentRepository;
    }

    @Cacheable(CacheConfig.CacheName.SUMMARY)
    public GetMonthlySummaryResponse getMonthSummary() {

        log.info("Summary not available in cache");
        List<IExpenseMonthSum> expenseSums = expenseRepository.findSumGroupByMonth();
        List<Transaction> transactions = transactionRepository.findAll();
        List<Investment> investments = investmentRepository.findAll();

        // From June 2007 June to May 2019 don't have expense entry - so lets add 0 every month later the
        // same will be overridden with actual value
        // This is needed because "expenseSums" collection is used to travers months
        TreeMap<Integer, IExpenseMonthSum> expenseMonthSumMap = new TreeMap<>(Comparator.reverseOrder());

        for (int year = 2007; year <= 2019; year++) {
            for (int month = 1; month <= 12; month++) {
                int finalYear = year;
                int finalMonth = month;
                expenseMonthSumMap.put(
                    Integer.valueOf(String.format("%d%02d", year, month)) ,
                    new IExpenseMonthSum() {
                        @Override
                        public Integer getYearx() {
                            return finalYear;
                        }

                        @Override
                        public Integer getMonthx() {
                            return finalMonth;
                        }

                        @Override
                        public Double getSum() {
                            return 0.0;
                        }
                    }
                );
            }
        }
        for (IExpenseMonthSum expenseMonthSum: expenseSums) {
            expenseMonthSumMap.put(
                    Integer.valueOf(String.format("%d%02d", expenseMonthSum.getYearx(), expenseMonthSum.getMonthx())),
                    expenseMonthSum
            );
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");

        // Salary aggregation
        Map<String, Double> monthlySalary = transactions.stream()
                .filter(Transaction::isSalary)
                .collect(
                        Collectors.groupingBy(
                                transaction -> df.format(transaction.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Transaction::getDebit),
                                        dss -> dss.getSum()
                                )
                        )
                );

        // Family transfer aggregation
        Map<String, Double> familyTransferMonthly = transactions.stream()
                .filter(transaction -> "Family".equals(transaction.getHead()) && transaction.getCredit() != null)
                .collect(
                        Collectors.groupingBy(
                                transaction -> df.format(transaction.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Transaction::getCredit),
                                        dss -> dss.getSum()
                                )
                        )
                );

        // Family received aggregation
        Map<String, Double> familyReceivedMonthly = transactions.stream()
                .filter(transaction -> "Family".equals(transaction.getHead()) && transaction.getDebit() != null)
                .collect(
                        Collectors.groupingBy(
                                transaction -> df.format(transaction.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Transaction::getDebit),
                                        dss -> dss.getSum()
                                )
                        )
                );

        // Investment aggregation
        Map<String, Long> investmentMonthly = investments.stream()
                .collect(
                        Collectors.groupingBy(
                                investment -> String.format("%d-%02d", investment.getYearx(), investment.getMonthx()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingInt(Investment::getContribution),
                                        iss -> iss.getSum()
                                )
                        )
                );

        // Expense aggregation
        List<GetMonthlySummaryResponse.MonthlySummary> monthSummaryRecord = expenseMonthSumMap.values().stream().
                map(
                        expenseMonthRecord -> GetMonthlySummaryResponse.MonthlySummary.builder()
                                .year(expenseMonthRecord.getYearx())
                                .month(expenseMonthRecord.getMonthx())
                                .expenseAmount(expenseMonthRecord.getSum())
                                .incomeAmount(
                                        monthlySalary.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx())))
                                .transferAmount(
                                        subtract(
                                                familyTransferMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx())),
                                                familyReceivedMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx()))
                                        )
                                )
                                .investmentAmount(
                                        investmentMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYearx(), expenseMonthRecord.getMonthx()))
                                )
                                .build()
                )
                .collect(Collectors.toList());


        return GetMonthlySummaryResponse.builder()
                .records(monthSummaryRecord)
                .build();
    }

    private Double subtract(Double a, Double b) {
        if (a == null)
            return b;

        if (b == null)
            return a;

        return a - b;
    }
}
