package com.alok.spring.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Investment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Short year;
    private Short month;
    private String head;
    private Integer contribution;
    private Integer valueAsOnMonth;

    public Integer getContribution() {
        return contribution==null?0:contribution;
    }

    public Integer getValueAsOnMonth() {
        return valueAsOnMonth==null?0:valueAsOnMonth;
    }

}
