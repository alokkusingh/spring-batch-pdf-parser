package com.alok.spring.stream;

import com.alok.spring.model.OdionTransaction;

import java.time.YearMonth;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class OdionAccountsMonthlyTransactionCollector implements Collector<OdionTransaction, Map<OdionTransaction.Account, Map<YearMonth, Double>>, Map<OdionTransaction.Account, Map<YearMonth, Double>>> {


    @Override
    public Supplier<Map<OdionTransaction.Account, Map<YearMonth, Double>>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<OdionTransaction.Account, Map<YearMonth, Double>>, OdionTransaction> accumulator() {
        return (monthAccountTransactionMap, transaction) -> {
            monthAccountTransactionMap.putIfAbsent(transaction.getDebitAccount(), new LinkedHashMap<>());
            monthAccountTransactionMap.putIfAbsent(transaction.getCreditAccount(), new LinkedHashMap<>());

            monthAccountTransactionMap.get(transaction.getCreditAccount()).compute(
                    YearMonth.of(transaction.getDate().minusDays(1).getYear(), transaction.getDate().minusDays(1).getMonth()),
                    (key, val) -> val == null? transaction.getAmount(): val + transaction.getAmount()
            );

            monthAccountTransactionMap.get(transaction.getDebitAccount()).compute(
                    YearMonth.of(transaction.getDate().minusDays(1).getYear(), transaction.getDate().minusDays(1).getMonth()),
                    (key, val) -> val == null? -transaction.getAmount(): val - transaction.getAmount()
            );
        };
    }

    @Override
    public BinaryOperator<Map<OdionTransaction.Account, Map<YearMonth, Double>>> combiner() {
        return null;
    }

    @Override
    public Function<Map<OdionTransaction.Account, Map<YearMonth, Double>>, Map<OdionTransaction.Account, Map<YearMonth, Double>>> finisher() {
        return monthAccountTransactionMap -> monthAccountTransactionMap;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
