package com.alok.spring.batch.processor;

import com.alok.spring.batch.model.RawTransaction;
import com.alok.spring.batch.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class CitiBankAccountProcessor implements ItemProcessor<RawTransaction, Transaction> {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyy");

    @Override
    public Transaction process(RawTransaction rawTransaction) throws ParseException {
        log.debug("Process line: {}",rawTransaction);

        Transaction transaction = new Transaction();

        transaction.setDate(extractDate(rawTransaction));

        transaction.setDescription(rawTransaction.getMergedLines());
        transaction.setSalary(isSalaryTransaction(transaction.getDescription()));

        if (transaction.isSalary()) {
            processSalaryTransaction(transaction);
        }

        return transaction;
    }

    private void processSalaryTransaction(Transaction transaction) {

    }

    private Date extractDate(RawTransaction rawTransaction) throws ParseException {
        String dateString = rawTransaction.getLines().get(0).substring(0,7);
        return simpleDateFormat.parse(dateString);
    }

    private boolean isSalaryTransaction(String transation) {
        if (transation.toLowerCase().matches(".*salary.*|.*evolving.*")) {
            return true;
        }

        return false;
    }
}


