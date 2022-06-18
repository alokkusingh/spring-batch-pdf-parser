package com.alok.spring.batch.processor;

import com.alok.spring.constant.MDCKey;
import com.alok.spring.model.RawTransaction;
import com.alok.spring.model.Transaction;
import com.alok.spring.batch.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.item.ItemProcessor;
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

        transaction.setBank(MDC.get(MDCKey.BANK.name()));
        if (transaction.isSalary()) {
            processSalaryTransaction(transaction);
            transaction.setSubHead(Utility.getCompanyName(transaction.getDescription()));
        } else if (Utility.isFamilyTransaction(transaction.getDescription())) {
            processFamilyTransaction(transaction);
        }

        return transaction;
    }

    protected void processFamilyTransaction(Transaction transaction) {
        transaction.setHead("Family");
    }

    protected void processSalaryTransaction(Transaction transaction) {
        if (transaction.getDebit() == null || transaction.getDebit() == 0) {
            // Don't consider as Salary
            return;
        }
        transaction.setHead("Salary");
    }

    private Date extractDate(RawTransaction rawTransaction) throws ParseException {
        String dateString = rawTransaction.getLines().get(0).substring(0,dateFiledLength);
        return simpleDateFormat.parse(dateString);
    }
}
