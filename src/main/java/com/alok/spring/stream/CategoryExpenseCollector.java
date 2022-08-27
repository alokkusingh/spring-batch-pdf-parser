package com.alok.spring.stream;

import com.alok.spring.model.Expense;
import com.alok.spring.response.GetExpensesResponseAggByDay;
import com.alok.spring.utils.DateUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class CategoryExpenseCollector implements Collector<Expense, Map<String, GetExpensesResponseAggByDay.CategoryExpense>, List<GetExpensesResponseAggByDay.CategoryExpense>> {

    @Override
    public Supplier<Map<String, GetExpensesResponseAggByDay.CategoryExpense>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<String, GetExpensesResponseAggByDay.CategoryExpense>, Expense> accumulator() {
        return (expenseCategoryMap, expense) -> {
            expenseCategoryMap.putIfAbsent(
                    expense.getCategory(),
                    GetExpensesResponseAggByDay.CategoryExpense.builder()
                            .expenses(new ArrayList<>())
                            .category(expense.getCategory())
                            .amount(0d)
                            .build()
            );

            GetExpensesResponseAggByDay.CategoryExpense categoryExpense = expenseCategoryMap.get(expense.getCategory());
            categoryExpense.getExpenses().add(GetExpensesResponseAggByDay.Expense.builder()
                    .date(DateUtils.convertToLocalDateViaInstant(expense.getDate()))
                    .amount(expense.getAmount())
                    .head(expense.getHead())
                    .comment(expense.getComment())
                    .build());
            categoryExpense.setAmount(categoryExpense.getAmount() + expense.getAmount());
        };
    }

    @Override
    public BinaryOperator<Map<String, GetExpensesResponseAggByDay.CategoryExpense>> combiner() {
        return null;
    }

    @Override
    public Function<Map<String, GetExpensesResponseAggByDay.CategoryExpense>, List<GetExpensesResponseAggByDay.CategoryExpense>> finisher() {

        return (categoryExpensesMap) -> new ArrayList<>(categoryExpensesMap.values());
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
