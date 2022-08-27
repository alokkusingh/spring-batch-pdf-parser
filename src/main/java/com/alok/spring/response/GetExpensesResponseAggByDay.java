package com.alok.spring.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class GetExpensesResponseAggByDay {

    private List<DayExpense> expenses;
    private List<CategoryExpense> categoryExpenses;
    private Integer count;
    private Date lastTransactionDate;

    @Data
    @Builder
    public static class DayExpense implements Comparable<DayExpense> {
        private LocalDate date;
        private Double amount;
        private List<Expense> expenses;

        @Override
        public int compareTo(DayExpense o) {
            return o.getDate().compareTo(date);
        }
    }

    @Data
    @Builder
    public static class CategoryExpense implements Comparable<CategoryExpense> {
        private String category;
        private Double amount;
        private List<Expense> expenses;

        @Override
        public int compareTo(CategoryExpense o) {
            return o.getCategory().compareTo(category);
        }
    }

    @Data
    @Builder
    public static class Expense {
        private LocalDate date;
        private String head;
        private String comment;
        private Double amount;
    }
}
