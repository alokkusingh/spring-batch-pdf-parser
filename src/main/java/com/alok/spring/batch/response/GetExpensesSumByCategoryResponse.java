package com.alok.spring.batch.response;

import com.alok.spring.batch.model.IExpenseCategorySum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetExpensesSumByCategoryResponse {

    private List<IExpenseCategorySum> expenseCategorySums;
    private Integer count;
}
