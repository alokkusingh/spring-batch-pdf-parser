package com.alok.spring.stream;

import com.alok.spring.model.Expense;
import com.alok.spring.response.GetExpensesResponseAggByDay;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class DayExpenseCollector implements Collector<Expense, Map<Date, GetExpensesResponseAggByDay.DayExpense>, List<GetExpensesResponseAggByDay.DayExpense>> {

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