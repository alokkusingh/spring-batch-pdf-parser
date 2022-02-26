package com.alok.spring.batch.response;

import com.alok.spring.batch.model.ExpenseCategorySum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class GetExpensesSumByCategoryResponse {

    private List<ExpenseCategorySum> expenseCategorySums;
    private Integer count;
}
