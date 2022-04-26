package com.alok.spring.batch.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class GetExpensesResponseAggByDay {

    private List<DayExpense> expenses;
    private Integer count;
    private Date lastTransactionDate;

    @Data
    @Builder
    public static class DayExpense implements Comparable<DayExpense> {
        private Date date;
        private Double amount;

        @Override
        public int compareTo(DayExpense o) {
            return o.getDate().compareTo(date);
        }
    }
}
