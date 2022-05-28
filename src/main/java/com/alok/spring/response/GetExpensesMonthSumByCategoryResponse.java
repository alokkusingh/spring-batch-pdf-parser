package com.alok.spring.response;

import com.alok.spring.model.IExpenseCategoryMonthSum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetExpensesMonthSumByCategoryResponse {

    private List<IExpenseCategoryMonthSum> expenseCategorySums;
    private Integer count;
}
