package com.alok.spring.stream;

import com.alok.spring.model.OdionTransaction;
import com.alok.spring.response.GetExpensesResponseAggByDay;
import com.alok.spring.utils.DateUtils;
import org.javatuples.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class OdionAccountsBalanceCollector implements Collector<OdionTransaction, Pair<Map<OdionTransaction.Account, Double>, Map<OdionTransaction.Account, Double>>, Map<OdionTransaction.Account, Double>> {


    @Override
    public Supplier<Pair<Map<OdionTransaction.Account, Double>, Map<OdionTransaction.Account, Double>>> supplier() {
        return () -> new Pair<>(new HashMap<>(), new HashMap<>());
    }

    @Override
    public BiConsumer<Pair<Map<OdionTransaction.Account, Double>, Map<OdionTransaction.Account, Double>>, OdionTransaction> accumulator() {
        return (debitCreditBalanceMapPair, transaction) -> {
            Map<OdionTransaction.Account, Double> debitBalanceMap = debitCreditBalanceMapPair.getValue0();
            Map<OdionTransaction.Account, Double> creditBalanceMap = debitCreditBalanceMapPair.getValue1();

            //debitBalanceMap.putIfAbsent(transaction.getDebitAccount(), 0.0D);
            //creditBalanceMap.putIfAbsent(transaction.getCreditAccount(), 0.0D);

            debitBalanceMap.compute(transaction.getDebitAccount(),
                    (account, amount) -> amount == null? transaction.getAmount(): amount + transaction.getAmount()
            );
            creditBalanceMap.compute(transaction.getCreditAccount(),
                    (account, amount) -> amount == null? transaction.getAmount(): amount + transaction.getAmount()
            );
        };
    }

    @Override
    public BinaryOperator<Pair<Map<OdionTransaction.Account, Double>, Map<OdionTransaction.Account, Double>>> combiner() {
        return null;
    }

    @Override
    public Function<Pair<Map<OdionTransaction.Account, Double>, Map<OdionTransaction.Account, Double>>, Map<OdionTransaction.Account, Double>> finisher() {
        return (debitCreditBalanceMapPair) -> {
            Map<OdionTransaction.Account, Double> debitBalanceMap = debitCreditBalanceMapPair.getValue0();
            Map<OdionTransaction.Account, Double> creditBalanceMap = debitCreditBalanceMapPair.getValue1();

            Map<OdionTransaction.Account, Double> accountBalanceMap = new HashMap<>();
            accountBalanceMap.putAll(debitBalanceMap);

            for (var entry: creditBalanceMap.entrySet()) {
                accountBalanceMap.compute(entry.getKey(), (account, amount) -> amount == null? -entry.getValue(): amount - entry.getValue());
            }

            return accountBalanceMap;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
