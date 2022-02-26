package com.alok.spring.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCategorySum {

    private String month;
    private String category;
    private Double sum;
}
