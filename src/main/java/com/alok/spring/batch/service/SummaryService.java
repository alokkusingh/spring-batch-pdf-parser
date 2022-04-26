package com.alok.spring.batch.service;

import com.alok.spring.batch.model.IExpenseMonthSum;
import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.repository.ExpenseRepository;
import com.alok.spring.batch.repository.TransactionRepository;
import com.alok.spring.batch.response.GetMonthlySummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SummaryService {

    private ExpenseRepository expenseRepository;
    private TransactionRepository transactionRepository;

    public SummaryService(ExpenseRepository expenseRepository, TransactionRepository transactionRepository) {
        this.expenseRepository = expenseRepository;
        this.transactionRepository = transactionRepository;
    }

    public GetMonthlySummaryResponse getMonthSummary() {

        List<IExpenseMonthSum> expenseSums = expenseRepository.findSumGroupByMonth();
        List<Transaction> transactions = transactionRepository.findAll();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");

        // Salary aggregation
        Map<String, Double> monthlySalary = transactions.stream()
                .filter(transaction -> transaction.isSalary())
                .collect(
                        Collectors.groupingBy(transaction -> df.format(transaction.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Transaction::getDebit),
                                        dss -> dss.getSum()
                                )
                        )
                );

        // Family transfer aggregation
        Map<String, Double> familyTransferMonthly = transactions.stream()
                .filter(transaction -> {
                    return "Family".equals(transaction.getHead()) && transaction.getCredit() != null;
                })
                .collect(
                        Collectors.groupingBy(transaction -> df.format(transaction.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Transaction::getCredit),
                                        dss -> dss.getSum()
                                )
                        )
                );

        // Family received aggregation
        Map<String, Double> familyReceivedMonthly = transactions.stream()
                .filter(transaction -> {
                    return "Family".equals(transaction.getHead()) && transaction.getDebit() != null;
                })
                .collect(
                        Collectors.groupingBy(transaction -> df.format(transaction.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Transaction::getDebit),
                                        dss -> dss.getSum()
                                )
                        )
                );

        List<GetMonthlySummaryResponse.MonthlySummary> monthSummaryRecord = expenseSums.stream().
                map(
                        expenseMonthRecord -> GetMonthlySummaryResponse.MonthlySummary.builder()
                                .year(expenseMonthRecord.getYear())
                                .month(expenseMonthRecord.getMonth())
                                .expenseAmount(expenseMonthRecord.getSum())
                                .incomeAmount(
                                        monthlySalary.get(String.format("%d-%02d", expenseMonthRecord.getYear(), expenseMonthRecord.getMonth())))
                                .transferAmount(
                                        subtract(
                                                familyTransferMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYear(), expenseMonthRecord.getMonth())),
                                                familyReceivedMonthly.get(String.format("%d-%02d", expenseMonthRecord.getYear(), expenseMonthRecord.getMonth()))
                                        )
                                )
                                .build())
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
