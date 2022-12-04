package com.alok.spring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Investment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Short yearx;
    private Short monthx;
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
