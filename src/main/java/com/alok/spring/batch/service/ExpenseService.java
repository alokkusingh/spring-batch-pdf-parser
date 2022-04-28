package com.alok.spring.batch.service;

import com.alok.spring.batch.model.Expense;
import com.alok.spring.batch.model.IExpenseCategoryMonthSum;
import com.alok.spring.batch.model.IExpenseMonthSum;
import com.alok.spring.batch.repository.ExpenseRepository;
import com.alok.spring.batch.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    public GetExpensesResponse getAllExpenses() {

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

    public GetExpensesResponse getCurrentMonthExpenses() {

        LocalDate currentDate = LocalDate.now();
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

    public GetExpensesResponseAggByDay getCurrentMonthExpensesSumByDay() {

        Date lastExpenseDate = expenseRepository.findLastTransactionDate()
                .orElse(new Date());

        LocalDate currentDate = LocalDate.now();
        List<Expense> expenses = expenseRepository.findAllForCurrentMonth(currentDate.getYear(), currentDate.getMonthValue());
        Collections.sort(expenses, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        return GetExpensesResponseAggByDay.builder()
                .expenses(aggregateExpensesByDay(expenses))
                .categoryExpenses(aggregateExpensesByCategory(expenses))
                .lastTransactionDate(lastExpenseDate)
                .count(expenses.size())
                .build();
    }

    public GetExpensesMonthSumByCategoryResponse getMonthWiseExpenseCategorySum() {

        List<IExpenseCategoryMonthSum> expenseCategorySums = expenseRepository.findCategorySumGroupByMonth();

        return GetExpensesMonthSumByCategoryResponse.builder()
                .expenseCategorySums(expenseCategorySums)
                .count(expenseCategorySums.size())
                .build();
    }

    public GetExpensesMonthSumResponse getMonthWiseExpenseSum() {

        List<IExpenseMonthSum> expenseSums = expenseRepository.findSumGroupByMonth();

        return GetExpensesMonthSumResponse.builder()
                .expenseCategorySums(expenseSums)
                .count(expenseSums.size())
                .build();
    }

    private List<GetExpensesResponseAggByDay.DayExpense> aggregateExpensesByDay(List<Expense> expenses) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Map<String, Double> dayExpenses = expenses.stream()
                .collect(
                        Collectors.groupingBy(
                                expense -> df.format(expense.getDate()),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(Expense::getAmount),
                                        dss -> dss.getSum()
                                )
                        )
                );

        List<GetExpensesResponseAggByDay.DayExpense> expensesByDay = dayExpenses.entrySet().stream()
                .map(entry -> {
                            try {
                                return GetExpensesResponseAggByDay.DayExpense.builder()
                                        .date(df.parse(entry.getKey()))
                                        .amount(entry.getValue())
                                        .build();
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                )
                .collect(Collectors.toList());

        Collections.sort(expensesByDay);

        return expensesByDay;
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
