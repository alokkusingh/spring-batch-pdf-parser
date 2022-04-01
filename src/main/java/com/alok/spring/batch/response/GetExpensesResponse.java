package com.alok.spring.batch.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class GetExpensesResponse {

    private List<Expense> expenses;
    private Integer count;
    private LocalDate lastTransactionDate;

    @Data
    @Builder
    public static class Expense {
        private Long id;
        private LocalDate date;
        private String head;
        private Double amount;
        private String category;
        private String comment;
    }
}
