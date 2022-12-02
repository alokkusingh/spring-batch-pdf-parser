package com.alok.spring.response;

import com.alok.spring.model.IExpenseCategoryMonthSum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetExpensesMonthSumByCategoryResponse {

    private List<ExpenseCategoryMonthSum> expenseCategorySums;
    private Integer count;

    @Data
    @Builder
    public static class ExpenseCategoryMonthSum {
        Integer year;
        Integer month;
        String category;
        Double sum;

    }
}
