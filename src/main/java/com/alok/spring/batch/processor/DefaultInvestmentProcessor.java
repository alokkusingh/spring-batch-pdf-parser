package com.alok.spring.batch.processor;

import com.alok.spring.model.Investment;
import com.alok.spring.model.RawInvestment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("defaultInvestmentProcessor")
@Slf4j
public class DefaultInvestmentProcessor implements ItemProcessor<RawInvestment, List<Investment>> {

    @Override
    public List<Investment> process(RawInvestment record) {
        List<Investment> monthInvestments = new ArrayList<>(4);
        monthInvestments.add(Investment.builder()
                .year(record.getYear())
                .month(record.getMonth())
                .head("PF")
                .contribution(record.getPfContribution())
                .valueAsOnMonth(record.getPfValueAsOnMonth())
                .build());

        monthInvestments.add(Investment.builder()
                .year(record.getYear())
                .month(record.getMonth())
                .head("NPS")
                .contribution(record.getNpsContribution())
                .valueAsOnMonth(record.getNpsValueAsOnMonth())
                .build());

        monthInvestments.add(Investment.builder()
                .year(record.getYear())
                .month(record.getMonth())
                .head("LIC")
                .contribution(record.getLicContribution())
                .valueAsOnMonth(record.getLicValueAsOnMonth())
                .build());

        monthInvestments.add(Investment.builder()
                .year(record.getYear())
                .month(record.getMonth())
                .head("SHARE")
                .contribution(record.getShareContribution())
                .valueAsOnMonth(record.getShareValueAsOnMonth())
                .build());

        return monthInvestments;
    }
}
