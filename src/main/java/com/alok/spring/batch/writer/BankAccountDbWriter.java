package com.alok.spring.batch.writer;

import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BankAccountDbWriter implements ItemWriter<Transaction> {

    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public void write(List<? extends Transaction> records) throws Exception {
       /* records.stream()
                .sorted()
                .filter(Transaction::isSalary)
                .forEach(
                record -> log.debug("Parsed record: {}", record )
        );*/

        transactionRepository.saveAll(records);

    }
}
