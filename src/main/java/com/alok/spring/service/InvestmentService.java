package com.alok.spring.service;

import com.alok.spring.model.Investment;
import com.alok.spring.repository.InvestmentRepository;
import com.alok.spring.response.GetInvestmentsResponse;
import com.alok.spring.stream.CustomCollectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InvestmentService {

    @Autowired
    private InvestmentRepository investmentRepository;

    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");


    public GetInvestmentsResponse getAllInvestments() {
        log.info("All Investments not available in cache");

        List<Investment> investments = investmentRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        Short currentYear = Short.valueOf(DateTimeFormatter.ofPattern("yyyy").format(now));
        Short currentMonth = Short.valueOf(DateTimeFormatter.ofPattern("MM").format(now));

        return investments.stream()
                .filter(investment -> {
                    if (currentYear < investment.getYearx())
                        return false;
                    if (currentYear > investment.getYearx())
                        return true;
                    if (currentMonth < investment.getMonthx())
                        return false;
                    return true;
                })
                .collect(Collectors.collectingAndThen(
                        CustomCollectors.toMonthInvestmentList(),
                        monthInvestmentSummary -> {
                           return GetInvestmentsResponse.builder()
                                   .investmentAmount(monthInvestmentSummary.getValue0())
                                   .asOnValue(monthInvestmentSummary.getValue1())
                                   .monthInvestments(monthInvestmentSummary.getValue3())
                                   .investmentsByType(monthInvestmentSummary.getValue2())
                                   .build();
                        }
                ));
    }

    @Deprecated
    public GetInvestmentsResponse getAllInvestmentsX() {
        log.info("All Investments not available in cache");

        List<Investment> investments = investmentRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        Short currentYear = Short.valueOf(DateTimeFormatter.ofPattern("yyyy").format(now));
        Short currentMonth = Short.valueOf(DateTimeFormatter.ofPattern("MM").format(now));

        Map<String, GetInvestmentsResponse.MonthInvestment> monthInvestmentsMap = investments.stream()
                .filter(investment -> {
                    if (currentYear < investment.getYearx())
                        return false;
                    if (currentYear > investment.getYearx())
                        return true;
                    if (currentMonth < investment.getMonthx())
                        return false;
                    return true;
                })
                .collect(
                        Collectors.groupingBy(
                                investment -> String.format("%d-%02d", investment.getYearx(), investment.getMonthx()),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream()
                                                .map(investment -> GetInvestmentsResponse.MonthInvestment.Investment.builder()
                                                        .head(investment.getHead())
                                                        .investmentAmount(investment.getContribution())
                                                        .asOnValue(investment.getValueAsOnMonth())
                                                        .build()
                                                )
                                                .collect(
                                                        Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                investmentList -> {
                                                                    Long totalInvestments = investmentList.stream()
                                                                            .map(GetInvestmentsResponse.MonthInvestment.Investment::getInvestmentAmount)
                                                                            .map(Integer::longValue)
                                                                            .reduce(0L, (sum, curr) -> sum + (curr == null?0L:curr));

                                                                    Long totalAsOnValue = investmentList.stream()
                                                                            .map(GetInvestmentsResponse.MonthInvestment.Investment::getAsOnValue)
                                                                            .map(Integer::longValue)
                                                                            .reduce(0L, (sum, curr) -> sum + (curr == null?0L:curr));

                                                                    return GetInvestmentsResponse.MonthInvestment.builder()
                                                                            .investmentAmount(totalInvestments)
                                                                            .asOnValue(totalAsOnValue)
                                                                            .investments(investmentList)
                                                                            .build();
                                                                }

                                                        )
                                                )
                                )
                        )
                );

        AtomicReference<Long> totalInvestments = new AtomicReference<>(0L);
        // TODO this should be the last mon value not the sum
        AtomicReference<Long> totalValues = new AtomicReference<>(0L);
        List<GetInvestmentsResponse.MonthInvestment> monthInvestments = new ArrayList<>(300);
        monthInvestmentsMap.entrySet().forEach(
            entry -> {
                entry.getValue().setYearMonth(entry.getKey());
                totalInvestments.updateAndGet(v -> v + entry.getValue().getInvestmentAmount());
                totalValues.updateAndGet(v -> v + entry.getValue().getAsOnValue());
                monthInvestments.add(entry.getValue());
            }
        );
        Collections.sort(monthInvestments);

        return GetInvestmentsResponse.builder()
                .asOnValue(totalValues.get())
                .monthInvestments(monthInvestments)
                .investmentAmount(totalInvestments.get())
                .build();
    }

    @Transactional
    public void saveAllInvestments(List<Investment> investmentRecords) {
        log.info("Delete all the investments first");
        investmentRepository.deleteAll();

        log.info("Save all the investments");
        investmentRepository.saveAll(investmentRecords);
    }
}
