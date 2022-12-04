package com.alok.spring.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RawInvestment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Short year;
    private Short month;
    private Integer pfContribution;
    private Integer pfValueAsOnMonth;
    private Integer npsContribution;
    private Integer npsValueAsOnMonth;
    private Integer licContribution;
    private Integer licValueAsOnMonth;
    private Integer shareContribution;
    private Integer shareValueAsOnMonth;
}
