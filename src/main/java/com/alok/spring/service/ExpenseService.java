package com.alok.spring.service;

import com.alok.spring.config.CacheConfig;
import com.alok.spring.model.Expense;
import com.alok.spring.model.IExpenseCategoryMonthSum;
import com.alok.spring.model.IExpenseMonthSum;
import com.alok.spring.repository.ExpenseRepository;
import com.alok.spring.response.GetExpensesMonthSumByCategoryResponse;
import com.alok.spring.response.GetExpensesMonthSumResponse;
import com.alok.spring.response.GetExpensesResponse;
import com.alok.spring.response.GetExpensesResponseAggByDay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "#root.methodName")
    public GetExpensesResponse getAllExpenses() {
        log.info("All Expenses not available in cache");

        List<Expense> expenses = expenseRepository.findAll();
        Date lastExpenseDate = expenseRepository.findLastTransactionDate()
                .orElse(new Date());
        Collections.sort(expenses, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        return GetExpensesResponse.builder()
                .expenses(expenses.stream()
                        .map(expense -> GetExpensesResponse.Expense.builder()
                                .id(expense.getId())
                                .date(expense.getDate())
                                .head(expense.getHead())
                                .amount(expense.getAmount())
                                .category(expense.getCategory())
                                .comment(expense.getComment())
                                .build())
                        .collect(Collectors.toList()))
                .count(expenses.size())
                .lastTransactionDate(lastExpenseDate)
                .build();
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "{ #root.methodName, #currentDate }")
    public GetExpensesResponse getCurrentMonthExpenses(LocalDate currentDate) {
        log.info("Current Month Expenses not available in cache");

        List<Expense> expenses = expenseRepository.findAllForCurrentMonth(currentDate.getYear(), currentDate.getMonthValue());
        Collections.sort(expenses, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        return GetExpensesResponse.builder()
                .expenses(expenses.stream()
                        .map(expense -> GetExpensesResponse.Expense.builder()
                                .id(expense.getId())
                                .date(expense.getDate())
                                .head(expense.getHead())
                                .amount(expense.getAmount())
                                .category(expense.getCategory())
                                .comment(expense.getComment())
                                .build())
                        .collect(Collectors.toList()))
                .count(expenses.size())
                .build();
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "{ #root.methodName, #currentDate }")
    public GetExpensesResponseAggByDay getCurrentMonthExpensesSumByDay(LocalDate currentDate) {
        log.info("Current Month Expenses Sum By Day not available in cache");

        Date lastExpenseDate = expenseRepository.findLastTransactionDate()
                .orElse(new Date());

        List<Expense> expenses = expenseRepository.findAllForCurrentMonth(currentDate.getYear(), currentDate.getMonthValue());
        Collections.sort(expenses, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        return GetExpensesResponseAggByDay.builder()
                .expenses(aggregateExpensesByDay(expenses))
                .categoryExpenses(aggregateExpensesByCategory(expenses))
                .lastTransactionDate(lastExpenseDate)
                .count(expenses.size())
                .build();
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "#root.methodName")
    public GetExpensesMonthSumByCategoryResponse getMonthWiseExpenseCategorySum() {
        log.info("Month wise Expenses Category Sum not available in cache");

        List<IExpenseCategoryMonthSum> expenseCategorySums = expenseRepository.findCategorySumGroupByMonth();

        return GetExpensesMonthSumByCategoryResponse.builder()
                .expenseCategorySums(expenseCategorySums)
                .count(expenseCategorySums.size())
                .build();
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "#root.methodName")
    public GetExpensesMonthSumResponse getMonthWiseExpenseSum() {
        log.info("Month wise Expenses Sum not available in cache");

        List<IExpenseMonthSum> expenseSums = expenseRepository.findSumGroupByMonth();

        return GetExpensesMonthSumResponse.builder()
                .expenseCategorySums(expenseSums)
                .count(expenseSums.size())
                .build();
    }

    private List<GetExpensesResponseAggByDay.DayExpense> aggregateExpensesByDay(List<Expense> expenses) {


        Map<String, Double> dayExpensesSum = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> df.format(expense.getDate()),
                        Collectors.collectingAndThen(
                                Collectors.summarizingDouble(Expense::getAmount),
                                DoubleSummaryStatistics::getSum
                        )
                    )
                );

        Map<String, List<GetExpensesResponseAggByDay.Expense>> dayExpenses = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> df.format(expense.getDate()),
                        Collectors.mapping(
                                expense -> GetExpensesResponseAggByDay.Expense.builder()
                                        .head(expense.getHead())
                                        .comment(expense.getComment())
                                        .amount(expense.getAmount())
                                        .build(),
                                Collectors.toList())
                        )
                );

        /*
        Map<String, GetExpensesResponseAggByDay.DayExpense> dayExpensesTest = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> df.format(expense.getDate()),
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                                DoubleSummaryStatistics dss = list.stream().collect(
                                        Collectors.summarizingDouble(Expense::getAmount)
                                );

                                List<GetExpensesResponseAggByDay.Expense> expenseRecords = list.stream().collect(
                                        Collectors.mapping(expense -> GetExpensesResponseAggByDay.Expense.builder()
                                                .head(expense.getHead())
                                                .comment(expense.getComment())
                                                .amount(expense.getAmount())
                                                .build(),
                                        Collectors.toList()));

                                Optional<Date> date = list.stream().findAny().map(Expense::getDate);

                                return GetExpensesResponseAggByDay.DayExpense.builder()
                                        .date(date.get())
                                        .amount(dss.getSum())
                                        .expenses(expenseRecords)
                                        .build();

                            }
                        )
                    )
                );
        */

        return dayExpensesSum.entrySet().stream()
                .map(entry -> GetExpensesResponseAggByDay.DayExpense.builder()
                        .date(strToDate(entry.getKey()))
                        .amount(entry.getValue())
                        .expenses(dayExpenses.get(entry.getKey()))
                        .build()
                )
                .sorted()
                .collect(Collectors.toList());
    }

    private Date strToDate(String dateStr) {
        try {
            return df.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private List<GetExpensesResponseAggByDay.CategoryExpense> aggregateExpensesByCategory(List<Expense> expenses) {

        Map<String, Double> catExpenses = expenses.stream()
                .collect(
                        Collectors.groupingBy(
                                Expense::getCategory,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        catExpList -> catExpList.stream()
                                                .map(Expense::getAmount)
                                                .reduce(0.0, (sub, amount) -> sub + amount)
                                )
                        )

                );

        List<GetExpensesResponseAggByDay.CategoryExpense> expenseByCategory = catExpenses.entrySet().stream()
                .map(entry -> GetExpensesResponseAggByDay.CategoryExpense.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        Collections.sort(expenseByCategory);

        return expenseByCategory;
    }

}
