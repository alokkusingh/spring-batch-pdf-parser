package com.alok.spring.batch.processor;

import com.alok.spring.batch.model.RawTransaction;
import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.utils.DefaultFieldExtractor;
import com.alok.spring.batch.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component("missingAccountProcessor")
@Slf4j
public class DefaultAccountProcessor implements ItemProcessor<Transaction, Transaction> {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyy");
    int dateFiledLength = 7;

    public void setDateFiledLength(int dateFiledLength) {
        this.dateFiledLength = dateFiledLength;
    }

    public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    @Override
    public Transaction process(Transaction transaction) throws ParseException {
        transaction.setSalary(Utility.isSalaryTransaction(transaction.getDescription()));

        if (transaction.isSalary()) {
            processSalaryTransaction(transaction);
        } else if (Utility.isFamilyTransaction(transaction.getDescription())) {
            processFamilyTransaction(transaction);
        }

        return transaction;
    }

    protected void processFamilyTransaction(Transaction transaction) {
        transaction.setHead("Family");
    }

    protected void processSalaryTransaction(Transaction transaction) {
        transaction.setHead("Salary");
    }

    private Date extractDate(RawTransaction rawTransaction) throws ParseException {
        String dateString = rawTransaction.getLines().get(0).substring(0,dateFiledLength);
        return simpleDateFormat.parse(dateString);
    }
}
