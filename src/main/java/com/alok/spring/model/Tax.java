package com.alok.spring.model;

import lombok.*;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
