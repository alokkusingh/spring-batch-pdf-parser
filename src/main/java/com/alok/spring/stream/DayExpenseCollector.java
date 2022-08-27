package com.alok.spring.stream;

import com.alok.spring.model.Expense;
import com.alok.spring.response.GetExpensesResponseAggByDay;
import com.alok.spring.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class DayExpenseCollector implements Collector<Expense, Map<LocalDate, GetExpensesResponseAggByDay.DayExpense>, List<GetExpensesResponseAggByDay.DayExpense>> {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        @Override
        public Supplier<Map<LocalDate, GetExpensesResponseAggByDay.DayExpense>> supplier() {
            return HashMap::new;
        }

        @Override
        public BiConsumer<Map<LocalDate, GetExpensesResponseAggByDay.DayExpense>, Expense> accumulator() {
            return (expenseDayMap, expense) -> {
                LocalDate date = DateUtils.convertToLocalDateViaInstant(expense.getDate());
                expenseDayMap.putIfAbsent(
                        date,
                        GetExpensesResponseAggByDay.DayExpense.builder()
                                .expenses(new ArrayList<>())
                                .amount(0d)
                                .date(date)
                                .build()
                );

                GetExpensesResponseAggByDay.DayExpense dayExpenses = expenseDayMap.get(date);
                dayExpenses.getExpenses().add(GetExpensesResponseAggByDay.Expense.builder()
                        .date(date)
                        .amount(expense.getAmount())
                        .head(expense.getHead())
                        .comment(expense.getComment())
                        .build());
                dayExpenses.setAmount(dayExpenses.getAmount() + expense.getAmount());
            };
        }

        @Override
        public BinaryOperator<Map<LocalDate, GetExpensesResponseAggByDay.DayExpense>> combiner() {
            return null;
        }

        @Override
        public Function<Map<LocalDate, GetExpensesResponseAggByDay.DayExpense>, List<GetExpensesResponseAggByDay.DayExpense>> finisher() {

            return (dayExpensesMap) -> new ArrayList<>(dayExpensesMap.values());
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.UNORDERED);
        }
    }