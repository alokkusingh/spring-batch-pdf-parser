package com.alok.spring.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetInvestmentsResponse {

    private Long totalInvestments;
    private Long totalValues;
    private List<MonthInvestment> investments;

    @Data
    @Builder
    public static class MonthInvestment implements Comparable<MonthInvestment> {
        String yearMonth;
        Long totalInvestments;
        Long totalValues;
        List<Investment> monthInvestments;

        @Override
        public int compareTo(MonthInvestment o) {
            return this.yearMonth.compareTo(o.yearMonth);
        }

        @Data
        @Builder
        public static class Investment {
            String head;
            Integer amount;
            Integer asOnValue;
        }
    }
}
