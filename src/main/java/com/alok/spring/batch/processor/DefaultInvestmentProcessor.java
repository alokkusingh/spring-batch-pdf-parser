package com.alok.spring.batch.processor;

import com.alok.spring.constant.InvestmentType;
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
                .yearx(record.getYear())
                .monthx(record.getMonth())
                .head(InvestmentType.PF.name())
                .contribution(record.getPfContribution())
                .valueAsOnMonth(record.getPfValueAsOnMonth())
                .build());

        monthInvestments.add(Investment.builder()
                .yearx(record.getYear())
                .monthx(record.getMonth())
                .head(InvestmentType.NPS.name())
                .contribution(record.getNpsContribution())
                .valueAsOnMonth(record.getNpsValueAsOnMonth())
                .build());

        monthInvestments.add(Investment.builder()
                .yearx(record.getYear())
                .monthx(record.getMonth())
                .head(InvestmentType.LIC.name())
                .contribution(record.getLicContribution())
                .valueAsOnMonth(record.getLicValueAsOnMonth())
                .build());

        monthInvestments.add(Investment.builder()
                .yearx(record.getYear())
                .monthx(record.getMonth())
                .head(InvestmentType.SHARE.name())
                .contribution(record.getShareContribution())
                .valueAsOnMonth(record.getShareValueAsOnMonth())
                .build());

        return monthInvestments;
    }
}
