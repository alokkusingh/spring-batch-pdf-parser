package com.alok.spring.service;

import com.alok.spring.config.CacheConfig;
import com.alok.spring.model.*;
import com.alok.spring.repository.ExpenseRepository;
import com.alok.spring.response.GetExpensesMonthSumByCategoryResponse;
import com.alok.spring.response.GetExpensesMonthSumResponse;
import com.alok.spring.response.GetExpensesResponse;
import com.alok.spring.response.GetExpensesResponseAggByDay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
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

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "{ #root.methodName, #yearMonth, #category }")
    public GetExpensesResponse getExpensesForMonth(java.time.YearMonth yearMonth, String category) {
        log.info("Current Month Expenses not available in cache");

        List<Expense> expenses = null;
        if (category == null)
           expenses = expenseRepository.findAllForMonth(yearMonth.getYear(), yearMonth.getMonthValue());
        else
            expenses = expenseRepository.findAllForMonthAndCategory(yearMonth.getYear(), yearMonth.getMonthValue(), category);

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

        List<Expense> expenses = expenseRepository.findAllForMonth(currentDate.getYear(), currentDate.getMonthValue());
        Collections.sort(expenses, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        return GetExpensesResponseAggByDay.builder()
                .expenses(aggregateExpensesByDayX(expenses))
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

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "{ #root.methodName, #category }")
    public GetExpensesMonthSumByCategoryResponse getMonthlyExpenseForCategory(String category) {
        log.info("Monthly was expense for category not available in cache");

        List<IExpenseCategoryMonthSum> expenseCategorySums = expenseRepository.findMonthlyExpenseForCategory(category);

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

    private List<GetExpensesResponseAggByDay.DayExpense> aggregateExpensesByDayX(List<Expense> expenses) {

        List<GetExpensesResponseAggByDay.DayExpense> expenseDaysSummary = expenses.stream()
                .collect(DayExpenseCollector.toDayExpenseList());

        return expenseDaysSummary;
    }

    static class DayExpenseCollector implements Collector<Expense, Map<Date, GetExpensesResponseAggByDay.DayExpense>, List<GetExpensesResponseAggByDay.DayExpense>> {

        public static DayExpenseCollector toDayExpenseList() {
            return new DayExpenseCollector();
        }


        @Override
        public Supplier<Map<Date, GetExpensesResponseAggByDay.DayExpense>> supplier() {
            return HashMap::new;
        }

        @Override
        public BiConsumer<Map<Date, GetExpensesResponseAggByDay.DayExpense>, Expense> accumulator() {
            return (expenseDayMap, expense) -> {
                if (!expenseDayMap.containsKey(expense.getDate())) {
                    expenseDayMap.put(
                            expense.getDate(),
                            GetExpensesResponseAggByDay.DayExpense.builder()
                                    .expenses(new ArrayList<>())
                                    .amount(0d)
                                    .date(expense.getDate())
                                    .build()
                    );
                }

                GetExpensesResponseAggByDay.DayExpense dayExpenses = expenseDayMap.get(expense.getDate());
                dayExpenses.getExpenses().add(GetExpensesResponseAggByDay.Expense.builder()
                                .amount(expense.getAmount())
                                .head(expense.getHead())
                                .comment(expense.getComment())
                        .build());
                dayExpenses.setAmount(dayExpenses.getAmount() + expense.getAmount());
            };
        }

        @Override
        public BinaryOperator<Map<Date, GetExpensesResponseAggByDay.DayExpense>> combiner() {
            return null;
        }

        @Override
        public Function<Map<Date, GetExpensesResponseAggByDay.DayExpense>, List<GetExpensesResponseAggByDay.DayExpense>> finisher() {

            return (dayExpensesMap) -> new ArrayList<>(dayExpensesMap.values());
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.UNORDERED);
        }
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
                                                .reduce(0.0, Double::sum)
                                )
                        )

                );

        return catExpenses.entrySet().stream()
                .map(entry -> GetExpensesResponseAggByDay.CategoryExpense.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .sorted()
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.CacheName.EXPENSE, key = "{ #root.methodName, #category }")
    public GetExpensesResponse getExpensesForCategory(String category) {
        log.info("Expenses for Category not available in cache");

        List<Expense> expenses = expenseRepository.findAllForCategory(category);
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

    public List<String> getExpenseCategories() {
        return expenseRepository.findDistinctCategories();
    }

    public List<YearMonth> getExpenseMonths() {
        List<YearMonth> yearMonths = expenseRepository.findDistinctYearMonths();
        Collections.sort(yearMonths);

        return yearMonths;
    }

    @Transactional
    public void saveAllExpenses(List<Expense> expenseRecords) {
        log.info("Delete all the expenses first");
        expenseRepository.deleteAll();

        log.info("Save all the expenses");
        expenseRepository.saveAll(expenseRecords);
    }
}
