package com.alok.spring.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class GetInvestmentsResponse {

    private Long investmentAmount;
    private Long asOnValue;
    private Map<String, Long> investmentsByType;
    private List<MonthInvestment> monthInvestments;

    @Data
    @Builder
    public static class MonthInvestment implements Comparable<MonthInvestment> {
        String yearMonth;
        Long investmentAmount;
        Long asOnInvestment;
        Long asOnValue;
        List<Investment> investments;

        @Override
        public int compareTo(MonthInvestment o) {
            return this.yearMonth.compareTo(o.yearMonth);
        }

        @Data
        @Builder
        public static class Investment {
            String head;
            Integer investmentAmount;
            Integer asOnInvestment;
            Integer asOnValue;
        }
    }
}
