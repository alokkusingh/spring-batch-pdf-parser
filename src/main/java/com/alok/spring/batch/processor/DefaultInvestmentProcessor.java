package com.alok.spring.batch.processor;

import com.alok.spring.model.Investment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@Component("defaultInvestmentProcessor")
@Slf4j
public class DefaultInvestmentProcessor implements ItemProcessor<Investment, Investment> {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyy");
    int dateFiledLength = 7;

    public void setDateFiledLength(int dateFiledLength) {
        this.dateFiledLength = dateFiledLength;
    }

    public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    @Override
    public Investment process(Investment transaction) throws ParseException {
        return transaction;
    }
}
