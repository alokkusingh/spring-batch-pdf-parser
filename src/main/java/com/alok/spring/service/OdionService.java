package com.alok.spring.service;

import com.alok.spring.model.OdionTransaction;
import com.alok.spring.repository.OdionTransactionRepository;
import com.alok.spring.response.GetOdionAccountTransactionsResponse;
import com.alok.spring.response.GetOdionAccountsBalanceResponse;
import com.alok.spring.response.GetOdionMonthlyAccountTransactionResponse;
import com.alok.spring.response.GetOdionTransactionsResponse;
import com.alok.spring.stream.CustomCollectors;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

@Slf4j
@Service
public class OdionService {

    private final OdionTransactionRepository odionTransactionRepository;

    public OdionService(OdionTransactionRepository odionTransactionRepository) {
        this.odionTransactionRepository = odionTransactionRepository;
    }

    public GetOdionTransactionsResponse getAllTransactions() {
        log.info("Get all Odion Transaction not in cache");

        return GetOdionTransactionsResponse.builder()
                .transactions(odionTransactionRepository.findAll().stream()
                        .filter(transaction -> transaction.getAmount() > 0)
                        .toList())
                .build();
    }

    public GetOdionAccountTransactionsResponse getAllTransactions(OdionTransaction.Account account) {
        log.info("Get all Odion Account Transaction not in cache");


        return GetOdionAccountTransactionsResponse.builder()
                .transactions(
                        odionTransactionRepository.findTransactionsForAccount(account).stream()
                                .collect(Collector.<OdionTransaction, List<GetOdionAccountTransactionsResponse.AccountTransaction>, List<GetOdionAccountTransactionsResponse.AccountTransaction>>of(
                                        ArrayList::new,
                                        (accountTransactions, transaction) -> {
                                            accountTransactions.add(
                                                    GetOdionAccountTransactionsResponse.AccountTransaction.builder()
                                                            .id(transaction.getId())
                                                            .date(transaction.getDate())
                                                            .particular(
                                                                    transaction.getParticular() +
                                                                            (transaction.getDebitAccount() == account?  " to " + transaction.getCreditAccount().toString() :  " from " + transaction.getDebitAccount().toString())
                                                            )
                                                            .debit(transaction.getDebitAccount() == account? transaction.getAmount() : 0)
                                                            .credit(transaction.getCreditAccount() == account? transaction.getAmount() : 0)
                                                            .build()
                                            );
                                        },
                                        (accountTransactionsPart1, accountTransactionsPart2) -> null,
                                        accountTransactions -> accountTransactions
                                ))
                )
                .build();
    }

    public GetOdionAccountsBalanceResponse getAllAccountBalance() {
        log.info("Get all Odion Account balance not in cache");

        List<OdionTransaction> transactions = odionTransactionRepository.findAll().stream()
                .filter(transaction -> transaction.getAmount() > 0)
                .toList();

        Map<OdionTransaction.Account, Double> accountBalanceMap = transactions.stream().collect(CustomCollectors.toOdionAccountsBalanceCollector());

        List<GetOdionAccountsBalanceResponse.AccountBalance> accountBalances = new ArrayList<>();
        for (var entry: accountBalanceMap.entrySet()) {
            accountBalances.add(GetOdionAccountsBalanceResponse.AccountBalance.builder()
                    .account(entry.getKey())
                    .balance(entry.getValue()).build());
        }

        return GetOdionAccountsBalanceResponse.builder()
            .accountBalances(accountBalances)
            .build();
    }

    public GetOdionMonthlyAccountTransactionResponse getMonthlyAccountTransaction() {
        log.info("Get monthly Odion Account transaction not in cache");

        return GetOdionMonthlyAccountTransactionResponse.builder()
                .accountMonthTransaction(odionTransactionRepository.findAll().stream()
                                .sorted(Comparator.comparing(OdionTransaction::getDate).reversed())
                                .filter(transaction -> transaction.getAmount() > 0)
                                .collect(CustomCollectors.toOdionAccountsMonthlyTransactionCollector())
                        )
                .build();
    }

    @Transactional
    public void saveAllTransactions(List<OdionTransaction> transactions) {
        log.info("Delete all the Odion transactions first");
        odionTransactionRepository.deleteAll();

        log.info("Save all the Odion transactions");
        odionTransactionRepository.saveAll(transactions);
    }
}
