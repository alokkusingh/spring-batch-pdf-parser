package com.alok.spring.batch.response;

import com.alok.spring.batch.model.IExpenseMonthSum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetExpensesMonthSumResponse {

    private List<IExpenseMonthSum> expenseCategorySums;
    private Integer count;
}
