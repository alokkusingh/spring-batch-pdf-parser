package com.alok.spring.response;

import com.alok.spring.model.IExpenseMonthSum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetExpensesMonthSumResponse {

    private List<IExpenseMonthSum> expenseCategorySums;
    private Integer count;
}
