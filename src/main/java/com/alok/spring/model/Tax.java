package com.alok.spring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Tax implements Comparable<Tax> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String financialYear;
    private Integer paidAmount;
    private Integer refundAmount;

    @Override
    public int compareTo(Tax o) {
        return this.financialYear.compareTo(o.financialYear);
    }

}
