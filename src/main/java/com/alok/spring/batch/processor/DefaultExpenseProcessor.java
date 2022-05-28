package com.alok.spring.batch.processor;

import com.alok.spring.model.Expense;
import com.alok.spring.model.RawTransaction;
import com.alok.spring.batch.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component("defaultExpenseProcessor")
@Slf4j
public class DefaultExpenseProcessor implements ItemProcessor<Expense, Expense> {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyy");
    int dateFiledLength = 7;

    public void setDateFiledLength(int dateFiledLength) {
        this.dateFiledLength = dateFiledLength;
    }

    public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    @Override
    public Expense process(Expense transaction) throws ParseException {
        setExpenseCategory(transaction);

        // the date input in the csv is in IST format but ??? utilis think the date is in UTC
        // to correct this deducting 05:30 hours for IST time
        fixDate(transaction);

        return transaction;
    }

    protected void setExpenseCategory(Expense transaction) {
        transaction.setCategory(Utility.getExpenseCategory(transaction.getHead(), transaction.getComment()));
    }

    protected void fixDate(Expense transaction) {
        Date oldDate = transaction.getDate();

        long istTime = oldDate.getTime() - 19800000;
        Date newDate = new Date(istTime);
        transaction.setDate(newDate);
    }


    private Date extractDate(RawTransaction rawTransaction) throws ParseException {
        String dateString = rawTransaction.getLines().get(0).substring(0,dateFiledLength);
        return simpleDateFormat.parse(dateString);
    }
}
