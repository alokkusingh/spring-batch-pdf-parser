package com.alok.spring.batch.service;

import com.alok.spring.batch.model.Expense;
import com.alok.spring.batch.repository.ExpenseRepository;
import com.alok.spring.batch.response.GetExpensesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    public GetExpensesResponse getAllExpenses() {


        List<Expense> expenses = expenseRepository.findAll();
        Collections.sort(expenses, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        List<GetExpensesResponse.Expense> transactionsList = expenses.stream()
                .map(expense -> GetExpensesResponse.Expense.builder()
                        .id(expense.getId())
                        .date(expense.getDate())
                        .head(expense.getHead())
                        .amount(expense.getAmount())
                        .category(expense.getComment())
                        .comment(expense.getComment())
                        .build())
                .collect(Collectors.toList());

        return GetExpensesResponse.builder()
                .expenses(transactionsList)
                .count(transactionsList.size())
                .build();
    }
}
