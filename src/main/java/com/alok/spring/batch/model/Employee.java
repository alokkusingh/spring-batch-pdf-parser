package com.alok.spring.batch.model;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Employee {
    private Integer id;
    private String name;
    private String departmentCode;
    private String department;
    private String salary;
    private Date time;

}
