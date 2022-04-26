package com.alok.spring.batch.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class GetExpensesResponseAggByDay {

    private Map<String, Double> expenses;
    private Integer count;
    private Date lastTransactionDate;
}
