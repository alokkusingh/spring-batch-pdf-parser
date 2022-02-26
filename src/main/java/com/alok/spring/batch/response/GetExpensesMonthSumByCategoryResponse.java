package com.alok.spring.batch.response;

import com.alok.spring.batch.model.IExpenseCategoryMonthSum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetExpensesMonthSumByCategoryResponse {

    private List<IExpenseCategoryMonthSum> expenseCategorySums;
    private Integer count;
}
